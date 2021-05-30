/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.example;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import com.owlike.genson.Genson;

@DataType()
public class Event {

    private final static Genson genson = new Genson();

    public Event() {
    }

    public Event(String eventNo, String name, String location, String eventDate, int ticketPrice) {
        Name = name;
        Location = location;
        EventDate = eventDate;
        TicketPrice = ticketPrice;
        EventNo = eventNo;
    }

    @Property()
    private String EventNo;

    @Property()
    private String Name;

    @Property()
    private String Location;

    public String getEventNo() {
        return EventNo;
    }

    public void setEventNo(String eventNo) {
        EventNo = eventNo;
    }

    @Property()
    private String EventDate;

    @Property()
    private int TicketPrice;

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getEventDate() {
        return EventDate;
    }

    public void setEventDate(String eventDate) {
        EventDate = eventDate;
    }

    public int getTicketPrice() {
        return TicketPrice;
    }

    public void setTicketPrice(int ticketPrice) {
        TicketPrice = ticketPrice;
    }

    public String toJSONString() {
        return genson.serialize(this).toString();
    }

    public static Event fromJSONString(String json) {
        Event asset = genson.deserialize(json, Event.class);
        return asset;
    }
}
