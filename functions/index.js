const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
admin.initializeApp();

let transporter = nodemailer.createTransport({
    host: 'smtp.gmail.com',
    port: 465,
    secure: true,
    auth: {
        user: 'yyhanjum@gmail.com',
        pass: 'hanjum2021'
    }
});

exports.sendNotification = functions.firestore
    .document('users/{user}/notices/{notice}')
    .onCreate((docSnapshot, context) => {
        const notice = docSnapshot.data()
        const recipentId = notice['uid']

        return admin.firestore().doc('users/' + recipentId).get().then(userDoc => {
            const registrationTokens = userDoc.get('tokens')

            var notificationContent = ""
            switch(notice['kind']) {
                case 0: 
                notificationContent = "님이 당신의 일지를 좋아합니다." 
                break
                case 1: 
                notificationContent = "님이 당신의 일지에 댓글을 남겼습니다."
                break
            }

            const notificationBody = (!notice['content'])? "확인하려면 탭하시오." : notice['content']
            const payload = {
                notification: {
                    title: notice['senderName'] + notificationContent,
                    body: notificationBody,
                    image: notice['contentUrl']
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

exports.sendReportMail = functions.https.onCall((data, context) => {
    const type = data.type
    const contentId = data.contentId
    const commentId = data.commentId
    const uid = context.auth?.uid
    const email = context.auth?.token.email

    if (!context.auth) {
        throw new functions.https.HttpsError('failed-precondition', '로그인 인증 실패');
    }
    
    var reportContent = ""
    switch(type) {
        case 0: 
        reportContent = "게시글 신고" 
        break
        case 1: 
        reportContent = "댓글 신고"
        break
    }

    const mailOptions = {
        from: `yyhanjum@gmail.com`,
        to: 'richpinkr99@gmail.com',
        subject: '영양한줌 신고 접수',
        html: `<h1>${reportContent}</h1>
                <p>
                uid: <b>${uid}</b><br>
                email: <b>${email}</b><br>
                contentId: <b>${contentId}</b><br>
                commentId: <b>${commentId}</b><br>
                </p>`
    };

    return new Promise((res, rej) => {
        transporter.sendMail(mailOptions, (error, data) => {
            if (error) {
                console.log(error.toString())
                console.log(data.toString())
                throw new functions.https.HttpsError('failed-sendMail', '이메일 발송 실패');
            }
            else {
                res()
            }
        })
    })
});