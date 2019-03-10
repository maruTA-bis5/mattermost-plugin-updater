/*
 * Copyright 2019 Takayuki Maruyama
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.bis5.mattermost.pluginupdater;

import org.springframework.boot.ExitCodeGenerator;

public class ExitCodeException extends RuntimeException implements ExitCodeGenerator {

    private static final long serialVersionUID = 7880618029057619936L;
    private final ExitReason reason;

    public ExitCodeException(ExitReason reason) {
        this(reason, "");
    }

    public ExitCodeException(ExitReason reason, String message) {
        super(reason.getDetail() + " " + message);
        this.reason = reason;
    }

    enum ExitReason {
        CONFIG_NOT_FOUND(1, "Configuration file is not found."), //
        INVALID_SETTING(2, "Missing variable: "), //
        FAILURE_PLUGIN_UPLOAD(3, "Failure plugin upload.");

        private final int exitCode;
        private final String detail;

        ExitReason(int exitCode, String detail) {
            this.exitCode = exitCode;
            this.detail = detail;
        }

        public String getDetail() {
            return detail;
        }

        public int getExitCode() {
            return exitCode;
        }
    }

    @Override
    public int getExitCode() {
        return reason.getExitCode();
    }

}