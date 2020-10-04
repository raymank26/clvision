package com.github.clvision.dashboard

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

data class UserJson @JsonCreator constructor(
        @JsonProperty("id") val id: String,
        @JsonProperty("username") val username: String
)