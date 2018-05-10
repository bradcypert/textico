package com.bradcypert.textico.models

import java.util.Date

abstract class Message : MessageProperties(), Comparable<Message> {
    public override var number: String? = null

    public override var body: String? = null

    public override var timestamp: Date? = null

    public override var id: Int = 0

    public override var threadId: String? = null

    public override var messageType: Int = 0

    val senderId: String?
        get() = this.person

    val isSentByMe: Boolean
        get() = this.sentByMe


    fun hasBeenRead(): Boolean {
        return this.read
    }

    override fun compareTo(other: Message): Int {
        return if (this.timestamp == null || other.timestamp == null) {
            0
        } else this.timestamp!!.compareTo(other.timestamp)

    }
}
