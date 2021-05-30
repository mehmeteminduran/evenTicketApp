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
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;
import java.util.List;

import com.owlike.genson.Genson;

@Contract(name = "EventContract",
    info = @Info(title = "Event contract",
                description = "My Smart Contract",
                version = "0.0.1",
                license =
                        @License(name = "Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "event@example.com",
                                                name = "event",
                                                url = "http://event.me")))
@Default
public class EventContract implements ContractInterface {
    
    private final Genson genson = new Genson(); 

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void InitLedger(final Context ctx) {  
        CreateEvent(ctx, "event1", "name", "loc", "date", 300);

    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean EventExists(Context ctx, String eventNo) {
        byte[] buffer = ctx.getStub().getState(eventNo);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Event CreateEvent(Context ctx, String eventNo, String name, String location, String eventDate, int ticketPrice) {

        // input validations
        String errorMessage = null;
        if (eventNo == null || eventNo.equals("")) {
            errorMessage = String.format("Empty input: eventNo");
        }
        if (name == null || name.equals("")) {
            errorMessage = String.format("Empty input: name");
        }
        if (location == null || location.equals("")) {
            errorMessage = String.format("Empty input: location");
        }
        if (eventDate == null || eventDate.equals("")) {
            errorMessage = String.format("Empty input: eventDate");
        } 
        
        if (errorMessage != null) {
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, ErrorMessageTitles.INCOMPLETE_INPUT.toString());
        }

        // Check if Event already exists
        boolean exists = EventExists(ctx, eventNo);
        if (exists) {
            errorMessage = String.format("Event %s already exists", eventNo);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, ErrorMessageTitles.ASSET_ALREADY_EXISTS.toString());
        }

        Event Event = new Event(eventNo,name, location, eventDate, ticketPrice);
        try {
            System.err.println("Event : " + Event.toJSONString());
            byte[] newEvent = Event.toJSONString().getBytes(UTF_8);
            ctx.getStub().putState(eventNo, newEvent);
        } catch (Exception e) {
            throw new ChaincodeException("chaincode error: " + e.getMessage(),
                    ErrorMessageTitles.DATA_ERROR.toString());
        }

        return Event;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Event ReadEvent(Context ctx, String eventNo) {
        boolean exists = EventExists(ctx, eventNo);
        if (!exists) {
            throw new RuntimeException("Event " + eventNo + " does not exist");
        }

        try {
            System.out.println(new String(ctx.getStub().getState(eventNo), UTF_8));
            Event event = Event
                    .fromJSONString(new String(ctx.getStub().getState(eventNo), UTF_8));
            return event;
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(),
                    ErrorMessageTitles.DATA_ERROR.toString());
        }

    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void UpdateEvent(Context ctx, String eventNo, String name, String location, String eventDate, int ticketPrice) {
 
         // input validations
         String errorMessage = null;
         if (eventNo == null || eventNo.equals("")) {
            errorMessage = String.format("Empty input: eventNo");
        }
         if (name == null || name.equals("")) {
             errorMessage = String.format("Empty input: name");
         }
         if (location == null || location.equals("")) {
             errorMessage = String.format("Empty input: location");
         }
         if (eventDate == null || eventDate.equals("")) {
             errorMessage = String.format("Empty input: eventDate");
         } 
         
         if (errorMessage != null) {
             System.err.println(errorMessage);
             throw new ChaincodeException(errorMessage, ErrorMessageTitles.INCOMPLETE_INPUT.toString());
         }

        boolean exists = EventExists(ctx, eventNo);
        if (!exists) {
            throw new RuntimeException("Event " + eventNo + " does not exist");
        }

        Event Event = new Event(eventNo,name, location, eventDate,ticketPrice);

        ctx.getStub().putState(eventNo, Event.toJSONString().getBytes(UTF_8));
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteEvent(Context ctx, String eventNo) {
        boolean exists = EventExists(ctx, eventNo);
        if (!exists) {
            throw new RuntimeException("Event " + eventNo + " does not exist");
        }
        ctx.getStub().delState(eventNo);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllEvents(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Event> queryResults = new ArrayList<Event>();

        // To retrieve all assets from the ledger use getStateByRange with empty startKey & endKey.
        // Giving empty startKey & endKey is interpreted as all the keys from beginning to end.
        // As another example, if you use startKey = 'asset0', endKey = 'asset9' ,
        // then getStateByRange will retrieve asset with keys between asset0 (inclusive) and asset9 (exclusive) in lexical order.
        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Event event = genson.deserialize(result.getStringValue(), Event.class);
            queryResults.add(event);
            System.out.println(event.toString());
        }

        final String response = genson.serialize(queryResults);

        return response;
    }

    private enum ErrorMessageTitles {
        INCOMPLETE_INPUT, INVALID_ACCESS, ASSET_NOT_FOUND, ASSET_ALREADY_EXISTS, DATA_ERROR
    }

 
}
