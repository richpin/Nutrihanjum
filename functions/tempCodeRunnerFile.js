const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();


exports.sendNotification = functions.firestore
    .document('users/{user}/notices/{notice}')
    .onCreate((docSnapshot, context) => {
        const notice = docSnapshot.data()
        const recipentId = notice['uid']

        return admin.firestore().doc('users/' + recipentId).get().then(userDoc => {
            const registrationTokens = userDoc.get('tokens')
            const senderName = userDoc.get('name')

            var notificationContent = "";
            switch(notice['kind']) {
                case '0': notificationContent = "님이 당신의 일지를 좋아합니다."
                case '1': notificationContent = "님이 당신의 일지에 댓글을 남겼습니다."
            }
            const payload = {
                notification: {
                    title: senderName + notificationContent,
                    clickAction: "MyPostActivity"
                },
                data: {
                   contentId: notice['contentId']                 
                }
            }

            return admin.messaging().sendToDevice(registrationTokens, payload).then( response => {
                const stillRegisteredTokens = registrationTokens
                
                response.results.forEach((result, index) => {
                    const error = result.error
                    if(error) {
                        const failedRegistrationToken = registrationTokens[index]
                        console.error('FCM Failed', failedRegistrationToken, error)
                        if(error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                            const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken)
                            if(failedIndex > -1){
                                stillRegisteredTokens.splice(failedIndex, 1)
                            }
                        }
                    } else {
                        console.log('FCM Success')
                    }
                })

                return admin.firestore().doc("users/" + recipentId)
                .update({registrationTokens: stillRegisteredTokens})
            })
        })
    })