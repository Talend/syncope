/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.syncope.common.lib;

public final class OIDCConstants {

    public static final String CLIENT_ID = "client_id";

    public static final String CLIENT_SECRET = "client_secret";

    public static final String ID_TOKEN_HINT = "id_token_hint";

    public static final String SCOPE = "scope";

    public static final String RESPONSE_TYPE = "response_type";

    public static final String STATE = "state";

    public static final String POST_LOGOUT_REDIRECT_URI = "post_logout_redirect_uri";

    public static final String REDIRECT_URI = "redirect_uri";

    public static final String CODE = "code";

    public static final String OP = "op";

    private OIDCConstants() {
        // private constructor for static utility class
    }

}
