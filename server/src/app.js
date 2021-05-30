const express = require("express");
const bodyParser = require("body-parser");
const cors = require("cors");
const morgan = require("morgan");
var xml = require("xml");

var network = require("./fabric/network.js");

const app = express();
app.use(morgan("combined"));
app.use(bodyParser.json());
app.use(cors());

app.get("/queryAllEvents", (req, res) => {
  network.queryAllEvents().then((response) => {
    var eventsRecord = JSON.parse(response);
    res.send(eventsRecord);
  });
});

app.post("/createEvent", (req, res) => {
  let id = "";
  var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  for (var i = 0; i < 8; i++) {
    id += chars.charAt(Math.floor(Math.random() * chars.length));
  }

  network
    .createEvent(
      id,
      req.body.name,
      req.body.location,
      req.body.eventDate,
      req.body.ticketPrice
    )
    .then((response) => {
      res.send(response);
    });
});

app.post("/updateEvent", (req, res) => {
  network
    .updateEvent(
      req.body.eventNo,
      req.body.name,
      req.body.location,
      req.body.eventDate,
      req.body.ticketPrice
    )
    .then((response) => {
      res.send(response);
    });
});

app.post("/deleteEvent", (req, res) => {
  network.deleteEvent(req.body.eventNo).then((response) => {
    res.send(response);
  });
});

app.get("/readEvent/:eventNo", (req, res) => {
  network.readEvent(req.params.eventNo).then((response) => {
    console.log(response);
    res.send(response);
  });
});

app.get("/queryAllTickets", (req, res) => {
  network.queryAllTickets().then((response) => {
    var ticketsRecord = JSON.parse(response);
    res.send(ticketsRecord);
  });
});

app.post("/createTicket", (req, res) => {
  let id = "";
  var chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  for (var i = 0; i < 8; i++) {
    id += chars.charAt(Math.floor(Math.random() * chars.length));
  }
  network
    .createTicket(
      id, 
      req.body.eventNo,
      req.body.owner,
      req.body.appraisedValue
    )
    .then((response) => {
      res.send(response);
    });
});

app.get("/readTicket/:ticketNo", (req, res) => {
  network.readEvent(req.params.ticketNo).then((response) => {
    console.log(response);
    res.send(response);
  });
});

app.post("/transferTicket", (req, res) => {
  network
    .transferTicket(req.body.ticketNo, req.body.newOwner, req.body.appraisedValue)
    .then((response) => {
      res.send(response);
    });
});
app.listen(process.env.PORT || 8081);
