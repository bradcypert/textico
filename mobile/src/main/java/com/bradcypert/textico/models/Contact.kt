package com.bradcypert.textico.models

abstract class ContactTrait {
    protected open var name: String? = null
    protected open var picUri: String? = null
    protected open var isStarred: Boolean = false
}

class Contact private constructor(name: String?, picUri: String?, isStarred: Boolean) : ContactTrait() {

    public override var name: String? = null
    public override var picUri: String? = null

    class ContactBuilder : ContactTrait() {

        fun setName(name: String?): ContactBuilder {
            this.name = name
            return this
        }

        fun setPicUri(picUri: String?): ContactBuilder {
            this.picUri = picUri
            return this
        }

        fun setStarred(starred: Boolean): ContactBuilder {
            this.isStarred = starred
            return this
        }

        fun build(): Contact {
            return Contact(name, picUri, isStarred)
        }
    }


    init {
        this.name = name
        this.picUri = picUri
        this.isStarred = isStarred
    }
}
