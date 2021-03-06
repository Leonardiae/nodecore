// VeriBlock NodeCore CLI
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package nodecore.cli.commands.rpc;

import com.google.gson.annotations.SerializedName;
import com.google.inject.Inject;
import io.grpc.StatusRuntimeException;
import nodecore.api.grpc.VeriBlockMessages;
import nodecore.cli.annotations.CommandSpec;
import nodecore.cli.commands.serialization.FormattableObject;
import nodecore.cli.contracts.Command;
import nodecore.cli.contracts.CommandContext;
import nodecore.cli.contracts.DefaultResult;
import nodecore.cli.contracts.Result;
import nodecore.cli.utilities.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

@CommandSpec(
        name = "Get Blockchains",
        form = "getblockchains",
        description = "Returns blockchain information")
public class GetBlockchainsCommand implements Command {
    private static final Logger _logger = LoggerFactory.getLogger(GetBlockchainsCommand.class);

    private class BlockchainPayload {
        @SerializedName("best_length")
        long bestLength;

        @SerializedName("longest_length")
        long longestLength;
    }

    @Inject
    public GetBlockchainsCommand() {
    }

    @Override
    public Result execute(CommandContext context) {
        Result result = new DefaultResult();
        try {
            VeriBlockMessages.GetBlockchainsReply reply = context
                    .adminService()
                    .getBlockchains(VeriBlockMessages.GetBlockchainsRequest
                            .newBuilder()
                            .build());
            if (!reply.getSuccess()) {
                result.fail();
            } else {
                FormattableObject<BlockchainPayload> temp = new FormattableObject<>(reply.getResultsList());
                temp.success = !result.didFail();
                temp.payload = new BlockchainPayload();
                temp.payload.bestLength = reply.getBestBlockchainLength();
                temp.payload.longestLength = reply.getLongestBlockchainLength();

                context.outputObject(temp);

                context.suggestCommands(Collections.singletonList(GetInfoCommand.class));
            }
            for (VeriBlockMessages.Result r : reply.getResultsList())
                result.addMessage(r.getCode(), r.getMessage(), r.getDetails(), r.getError());
        } catch (StatusRuntimeException e) {
            CommandUtility.handleRuntimeException(result, e, _logger);
        }
        return result;
    }
}
