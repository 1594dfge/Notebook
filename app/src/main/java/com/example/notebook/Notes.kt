package com.example.notebook

import java.time.LocalDateTime

class Notes(val uuid: String?, val title: String?, val content: String?, var isChecked: Boolean, val color: String?, val colorLevel: Int, val createDate: LocalDateTime, val updateDate: LocalDateTime)