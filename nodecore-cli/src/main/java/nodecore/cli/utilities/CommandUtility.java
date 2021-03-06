// VeriBlock NodeCore CLI
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package nodecore.cli.utilities;

import io.grpc.StatusRuntimeException;
import nodecore.cli.contracts.Result;
import org.slf4j.Logger;

public class CommandUtility {
    private static final String TLS_NO_SUBJ_ALT_NAMES_PRESENT = "No subject alternative names present";
    private CommandUtility(){}

    public static void handleRuntimeException(Result result, StatusRuntimeException e, Logger logger) {

        String resultMessage = "Remote service call failure";
        String resultDetails = e.toString();
        String resultCode = "V800";
        Throwable cause = e.getCause();
        while(cause != null) {
            logger.info("cause: {}", cause.getMessage());
            if(cause.getMessage().equals(TLS_NO_SUBJ_ALT_NAMES_PRESENT)) {
                resultMessage = cause.toString();
                resultDetails = "When connecting over a secured channel, the peer parameter must match the server's domain name. If the IP address of the server is used, the certificate must have the SAN attribute present, and it must match the IP address supplied to connect.";
                resultCode = "V900";
                cause = null;
            }
            else
                cause = cause.getCause();
        }

        result.addMessage(
                resultCode,
                resultMessage,
                resultDetails,
                true);
        result.fail();

        logger.error("V800: Remote service call failure", e);
    }
}
