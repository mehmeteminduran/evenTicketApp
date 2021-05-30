/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;

@DataType()
public class Ticket {

    private final static Genson genson = new Genson();

    public Ticket(){
    }

    
    public Ticket(String ticketNo, String eventNo, String owner, int appraisedValue) {
        this.ticketNo = ticketNo;
        this.eventNo = eventNo;
        this.owner = owner;
        this.appraisedValue = appraisedValue;
    }


    public String getTicketNo() {
        return ticketNo;
    }

    public void setTicketNo(String ticketNo) {
        this.ticketNo = ticketNo;
    }

    public String getEventNo() {
        return eventNo;
    }

    public void setEventNo(String eventNo) {
        this.eventNo = eventNo;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getAppraisedValue() {
        return appraisedValue;
    }

    public void setAppraisedValue(int appraisedValue) {
        this.appraisedValue = appraisedValue;
    }


    @Property()
    private String ticketNo;

    @Property()
    private String eventNo;

    @Property()
    private String owner;

    @Property()
    private int appraisedValue;

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public static Ticket fromJSONString(String json) {
        Ticket asset = genson.deserialize(json, Ticket.class);
        return asset;
    }
}
