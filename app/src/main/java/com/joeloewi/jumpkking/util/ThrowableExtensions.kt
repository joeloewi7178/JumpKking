package com.joeloewi.jumpkking.util

import com.google.firebase.firestore.FirebaseFirestoreException

fun Throwable.castToQuotaReachedExceptionAndGetMessage(): String =
    if (this is FirebaseFirestoreException && code == FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED) {
        "서버 일일 사용 한도에 도달하였습니다."
    } else {
        "오류가 발생했습니다."
    }