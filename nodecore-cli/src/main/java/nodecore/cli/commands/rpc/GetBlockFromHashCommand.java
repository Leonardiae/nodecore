// VeriBlock NodeCore CLI
// Copyright 2017-2019 Xenios SEZC
// All rights reserved.
// https://www.veriblock.org
// Distributed under the MIT software license, see the accompanying
// file LICENSE or http://www.opensource.org/licenses/mit-license.php.

package nodecore.cli.commands.rpc;

import com.google.inject.Inject;
import io.grpc.StatusRuntimeException;
import nodecore.api.grpc.VeriBlockMessages;
import nodecore.api.grpc.utilities.ByteStringUtility;
import nodecore.cli.annotations.CommandParameterType;
import nodecore.cli.annotations.CommandSpec;
import nodecore.cli.annotations.CommandSpecParameter;
import nodecore.cli.commands.serialization.BlocksPayload;
import nodecore.cli.commands.serialization.FormattableObject;
import nodecore.cli.contracts.Command;
import nodecore.cli.contracts.CommandContext;
import nodecore.cli.contracts.DefaultResult;
import nodecore.cli.contracts.Result;
import nodecore.cli.utilities.CommandUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

@CommandSpec(
        name = "Get Block from Hash",
        form = "getblockfromhash",
        description = "Returns the block for the specified hash")
@CommandSpecParameter(name = "blockHash", required = true, type = CommandParameterType.HASH)
public class GetBlockFromHashCommand implements Command {
    private static final Logger _logger = LoggerFactory.getLogger(GetBlockFromHashCommand.class);

    @Inject
    public GetBlockFromHashCommand() {
    }

    @Override
    public Result execute(CommandContext context) {
        Result result = new DefaultResult();

        String hash = context.getParameter("blockHash");
        try {
            VeriBlockMessages.GetBlocksRequest request = VeriBlockMessages.GetBlocksRequest
                    .newBuilder()
                    .addFilters(VeriBlockMessages.BlockFilter.newBuilder()
                            .setHash(ByteStringUtility.hexToByteString(hash)))
                    .build();
            VeriBlockMessages.GetBlocksReply reply = context.adminService().getBlocks(request);
            if (!reply.getSuccess()) {
                result.fail();
            } else {
                FormattableObject<BlocksPayload> temp = new FormattableObject<>(reply.getResultsList());
                temp.success = !result.didFail();
                temp.payload = new BlocksPayload(reply.getBlocksList());

                context.outputObject(temp);

                context.suggestCommands(Arrays.asList(
                        GetBlockFromIndexCommand.class,
                        GetTransactionCommand.class));
            }
            for (VeriBlockMessages.Result r : reply.getResultsList())
                result.addMessage(r.getCode(), r.getMessage(), r.getDetails(), r.getError());
        } catch (StatusRuntimeException e) {
            CommandUtility.handleRuntimeException(result, e, _logger);
        }

        return result;
    }
}
