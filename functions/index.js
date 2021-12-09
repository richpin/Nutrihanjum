const functions = require('firebase-functions');
const admin = require('firebase-admin');
const nodemailer = require('nodemailer');
const axios = require('axios');
const serviceAccount = require('./service_account.json');
const { auth } = require('firebase-admin');

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
});

const db = admin.firestore();

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

        return db.doc('users/' + recipentId).get().then(userDoc => {
            if (!userDoc.get("noticeFlag")) return

            const registrationTokens = userDoc.get('tokens')

            var notificationContent = ""
            switch (notice['kind']) {
                case 0:
                    notificationContent = "님이 당신의 일지를 좋아합니다."
                    break
                case 1:
                    notificationContent = "님이 당신의 일지에 댓글을 남겼습니다."
                    break
            }

            const notificationBody = (!notice['content']) ? "확인하려면 탭하시오." : notice['content']
            const payload = {
                notification: {
                    title: notice['senderName'] + notificationContent,
                    body: notificationBody,
                    image: notice['contentUrl']
                }
            }

            return admin.messaging().sendToDevice(registrationTokens, payload).then(response => {
                const stillRegisteredTokens = registrationTokens

                response.results.forEach((result, index) => {
                    const error = result.error
                    if (error) {
                        const failedRegistrationToken = registrationTokens[index]
                        console.error('FCM Failed', failedRegistrationToken, error)
                        if (error.code === 'messaging/invalid-registration-token' || error.code === 'messaging/registration-token-not-registered') {
                            const failedIndex = stillRegisteredTokens.indexOf(failedRegistrationToken)
                            if (failedIndex > -1) {
                                stillRegisteredTokens.splice(failedIndex, 1)
                            }
                        }
                    } else {
                        console.log('FCM Success')
                    }
                })

                return db.doc("users/" + recipentId)
                    .update({ registrationTokens: stillRegisteredTokens })
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
    switch (type) {
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

    transporter.sendMail(mailOptions, (error, data) => {
        if (error) {
            console.log(error.toString())
            console.log(data.toString())
            throw new functions.https.HttpsError('failed-sendMail', '이메일 발송 실패');
        }
    })
});

const kakaoRequestMeUrl = 'https://kapi.kakao.com/v2/user/me'
const naverRequestMeUrl = 'https://openapi.naver.com/v1/nid/me'

/**
 * requestMe - Returns user profile from Kakao API
 *
 * @param  {String} kakaoAccessToken Access token retrieved by Kakao Login API
 * @return {Promiise<Response>}      User profile response in a promise
 */
async function requestKakaoMe(kakaoAccessToken) {
    console.log('Requesting user profile from Kakao API server.')
    var result = await axios.get(kakaoRequestMeUrl, {
        method: 'GET',
        headers: { Authorization: 'Bearer ' + kakaoAccessToken }
    })
    return result
}

/**
 * requestMe - Returns user profile from Kakao API
 *
 * @param  {String} naverAccessToken Access token retrieved by Kakao Login API
 * @return {Promiise<Response>}      User profile response in a promise
 */
 async function requestNaverMe(naverAccessToken) {
    console.log('Requesting user profile from Naver API server.')
    var result = await axios.get(naverRequestMeUrl, {
        method: 'GET',
        headers: {
	    'Authorization': 'Bearer ' + naverAccessToken,
	    'X-Naver-Client-Id': 'mxrQwyd6MdXhiw6vOWPX',
	    'X-Naver-Client-Secret': 'sGExIUfD7b'}
	    })
    return result
}

async function updateOrCreateUser(updateParams) {
    console.log('updating or creating a firebase user');
    console.log(updateParams);
    try{
        var userRecord = await admin.auth().updateUser(updateParams.uid, updateParams)
    } catch (error) {
        if (error.code === 'auth/user-not-found') {
            userRecord = await admin.auth().createUser(updateParams)
                .then(async (newRecord) => {
                    const data = {
                        name: newRecord.displayName,
                        email: newRecord.email,
                        userID: newRecord.uid,
                        profileUrl: ""
                    }

                    if(newRecord.photoURL){
                        data.profileUrl = newRecord.photoURL
                    }

                    console.log(data)
                    const res = await db
                        .collection('users')
                        .doc(data.userID)
                        .set(data).catch((error) => {
                            console.log(error)
                        })
                })
        } throw error;
    }
    return userRecord
}

/**
 * createFirebaseToken - returns Firebase token using Firebase Admin SDK
 *
 * @param  {String} kakaoAccessToken access token from Kakao Login API
 * @return {Promise<String>}                  Firebase token in a promise
 */
async function createKakaoFirebaseToken(kakaoAccessToken) {
    var requestMeResult = await requestKakaoMe(kakaoAccessToken)
    const userData = requestMeResult.data
    console.log(userData)
    const userId = `kakao:${userData.id}`
    if (!userId) {
        return res.status(404)
            .send({ message: 'There was no user with the given access token.' })
    }
    let nickname = null
    let profileImage = null
    if (userData.properties) {
        nickname = userData.properties.nickname
        profileImage = userData.properties.profile_image
    }

    const updateParams = {
        uid: userId,
        provider: 'KAKAO',
        displayName: nickname,
        email: userData.kakao_account.email
    }

    if (profileImage) {
		updateParams['photoURL'] = profileImage;
	}

    var userRecord = await updateOrCreateUser(updateParams).catch((error) => {
        if(error.code === 'auth/email-already-exists'){
            console.log('email-already-exits')
            return new Promise(function(resolve, reject) {
                throw new functions.https.HttpsError('already-exists','해당 이메일로 가입된 다른 계정이 있습니다.')
              })
        }
    })

    return admin.auth().createCustomToken(userId, { provider: 'KAKAO' })
}

/**
 * createFirebaseToken - returns Firebase token using Firebase Admin SDK
 *
 * @param  {String} naverAccessToken access token from Kakao Login API
 * @return {Promise<String>}                  Firebase token in a promise
 */
 async function createNaverFirebaseToken(naverAccessToken) {
    var requestMeResult = await requestNaverMe(naverAccessToken)
    const userData = requestMeResult.data
    console.log(userData)
    const userId = `naver:${userData.response.id}`
    if (!userId) {
        return res.status(404)
            .send({ message: 'There was no user with the given access token.' })
    }
    let nickname = userData.response.nickname
    let profileImage = userData.response.profile_image

    const updateParams = {
        uid: userId,
        provider: 'NAVER',
        displayName: nickname,
        email: userData.response.email
    }

    if (profileImage) {
		updateParams['photoURL'] = profileImage;
	}

    var userRecord = await updateOrCreateUser(updateParams).catch((error) => {
        if(error.code === 'auth/email-already-exists'){
            console.log('email-already-exits')
            return new Promise(function(resolve, reject) {
                throw new functions.https.HttpsError('already-exists','해당 이메일로 가입된 다른 계정이 있습니다.')
              })
        }
    })

    return admin.auth().createCustomToken(userId, { provider: 'NAVER' })
}

exports.kakaoCustomAuth = functions.region('asia-northeast3').https.onCall((data, context) => {
    const token = data.token
    if (!token) return resp.status(400).send({ error: 'There is no token.' })
        .send({ message: 'Access token is a required parameter.' })

    console.log(`Verifying Kakao token: ${token}`)
    var firebaseToken = createKakaoFirebaseToken(token)
    return firebaseToken
})

exports.naverCustomAuth = functions.region('asia-northeast3').https.onCall((data, context) => {
    const token = data.token
    if (!token) return resp.status(400).send({ error: 'There is no token.' })
        .send({ message: 'Access token is a required parameter.' })

    console.log(`Verifying Naver token: ${token}`)
    var firebaseToken = createNaverFirebaseToken(token)
    return firebaseToken
})