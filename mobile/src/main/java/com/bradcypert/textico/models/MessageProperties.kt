package com.bradcypert.textico.models

import java.util.Date

abstract class MessageProperties {
    protected open var number: String? = null
    protected open var body: String? = null
    protected open var timestamp: Date? = null
    protected open var read: Boolean = false
    protected open var id: Int = 0
    protected open var threadId: String? = null
    protected open var person: String? = null
    protected open var sentByMe: Boolean = false
    protected open var isArchived = false
    // This is shitty but it is what it is
    // 0 = SMS; 1 = MMS
    protected open var messageType: Int = 0
}
