/*
 * Copyright (C) 2022 TianFeng
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tt.tt.constant

/**
 * @author tianfeng
 */
object TTConstant {
    const val REQ_PERMISSIONS = 0xF010
    const val REQ_AUTH = 0xF011
    const val REQ_DATA = 0xF020
    const val REQ_FILE = 0xF021
    const val REQ_IMAGE = 0xF022
    const val REQ_CAMERA = 0xF030
    const val REQ_CROP = 0xF031

    const val ARG_ID = "id"
    const val ARG_TYPE = "type"
    const val ARG_TITLE = "title"
    const val ARG_DATA = "data"
    const val ARG_EXTRA = "extra"
    const val ARG_RESULT = "result"
    const val ARG_REQUEST_CODE = "requestCode"
}