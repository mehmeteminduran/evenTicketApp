/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.ArrayList;
import java.util.List;

import com.owlike.genson.Genson;

@Contract(name = "TicketContract", info = @Info(title = "Ticket contract", description = "My Smart Contract", version = "0.0.1", license = @License(name = "Apache-2.0", url = ""), contact = @Contact(email = "ticket@example.com", name = "ticket", url = "http://ticket.me")))
@Default
public class TicketContract implements ContractInterface {

    private final Genson genson = new Genson();

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean TicketExists(Context ctx, String ticketNo) {
        byte[] buffer = ctx.getStub().getState(ticketNo);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Ticket CreateTicket(Context ctx, String ticketNo, String eventNo, String owner, int appraisedValue) {

        // input validations
        String errorMessage = null;
        if (ticketNo == null || ticketNo.equals("")) {
            errorMessage = String.format("Empty input: ticketNo");
        }
        if (eventNo == null || eventNo.equals("")) {
            errorMessage = String.format("Empty input: eventNo");
        }
        if (owner == null || owner.equals("")) {
            errorMessage = String.format("Empty input: owner");
        }

        if (errorMessage != null) {
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, ErrorMessageTitles.INCOMPLETE_INPUT.toString());
        }

        // Check if Ticket already exists
        boolean exists = TicketExists(ctx, ticketNo);
        if (exists) {
            errorMessage = String.format("Ticket %s already exists", ticketNo);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, ErrorMessageTitles.ASSET_ALREADY_EXISTS.toString());
        }

        Ticket Ticket = new Ticket(ticketNo, eventNo, owner, appraisedValue);
        try {
            System.err.println("Ticket : " + Ticket.toJSONString());
            byte[] newTicket = Ticket.toJSONString().getBytes(UTF_8);
            ctx.getStub().putState(ticketNo, newTicket);
        } catch (Exception e) {
            throw new ChaincodeException("chaincode error: " + e.getMessage(),
                    ErrorMessageTitles.DATA_ERROR.toString());
        }

        return Ticket;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Ticket ReadTicket(Context ctx, String ticketNo) {
        boolean exists = TicketExists(ctx, ticketNo);
        if (!exists) {
            throw new RuntimeException("Ticket " + ticketNo + " does not exist");
        }

        try {
            System.out.println(new String(ctx.getStub().getState(ticketNo), UTF_8));
            Ticket ticket = Ticket.fromJSONString(new String(ctx.getStub().getState(ticketNo), UTF_8));
            return ticket;
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(),
                    ErrorMessageTitles.DATA_ERROR.toString());
        }

    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllTickets(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Ticket> queryResults = new ArrayList<Ticket>();

        // To retrieve all assets from the ledger use getStateByRange with empty
        // startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning
        // to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive)
        // and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result : results) {
            
            Ticket ticket = genson.deserialize(result.getStringValue(), Ticket.class);
            queryResults.add(ticket);
            System.out.println(ticket.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Ticket TransferTicket(Context ctx, String ticketNo, final String newOwner, int appraisedValue) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(ticketNo);

        if (assetJSON == null || assetJSON.isEmpty()) {
            String errorMessage = String.format("Ticket %s does not exist", ticketNo);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, ErrorMessageTitles.ASSET_NOT_FOUND.toString());
        }

        Ticket ticket = genson.deserialize(assetJSON, Ticket.class);

        Ticket newTicket = new Ticket(ticket.getTicketNo(), ticket.getEventNo(), newOwner, appraisedValue);
        String newTicketSON = genson.serialize(newTicket);
        stub.putStringState(ticketNo, newTicketSON);

        return newTicket;
    }

    private enum ErrorMessageTitles {
        INCOMPLETE_INPUT, INVALID_ACCESS, ASSET_NOT_FOUND, ASSET_ALREADY_EXISTS, DATA_ERROR
    }

}
