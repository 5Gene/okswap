package osp.sparkj.more.okswap

import java.io.IOException


interface Callback<Response> {
    /**
     * Called when the request could not be executed due to cancellation, a connectivity problem or
     * timeout. Because networks can fail during an exchange, it is possible that the remote server
     * accepted the request before the failure.
     */
    fun onFailure(e: Exception)

    /**
     * Called when the HTTP response was successfully returned by the remote server. The callback may
     * proceed to read the response body with [Response.body]. The response is still live until its
     * response body is [closed][ResponseBody]. The recipient of the callback may consume the response
     * body on another thread.
     *
     * Note that transport-layer success (receiving a HTTP response code, headers and body) does not
     * necessarily indicate application-layer success: `response` may still indicate an unhappy HTTP
     * response code like 404 or 500.
     */
    @Throws(IOException::class)
    fun onResponse(response: Response)
}
