package com.example.stopgap.serde

import extension.InjectInstance
import extension.webservertest.SetupCapture
import extension.webservertest.WebserverTest
import io.helidon.webclient.api.WebClient

@WebserverTest
class CustomEndpointTest {

    @InjectInstance
    lateinit var client: WebClient

    companion object {
        @JvmStatic
        @WebserverTest.Setup
        fun setup(): SetupCapture {
            return SetupCapture(CustomEndpoint())
        }
    }
}
