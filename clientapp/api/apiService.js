import Api from '@/api/api'

export default {
  transferTicket(ticketNo, newOwner, appraisedValue) {
    return Api().post('transferTicket', {       
      ticketNo: ticketNo,
      newOwner: newOwner,
      appraisedValue : appraisedValue
    })
  },
  createTicket(eventNo, owner, appraisedValue) {
    return Api().post('createTicket', {       
      eventNo: eventNo,
      owner: owner,
      appraisedValue: appraisedValue 
    })
  },
  queryAllTickets() {
    return Api().get('queryAllTickets')
  },  
  readTicket(ticketNo) {
    return Api().get('readTicket', {params: {ticketNo: ticketNo}})
  },
  createEvent(name, location, eventDate, ticketPrice) {
    return Api().post('createEvent', {       
      name: name,
      location: location,
      eventDate: eventDate,
      ticketPrice: ticketPrice
    })
  },
  queryAllEvents() {
    return Api().get('queryAllEvents')
  },  
  readEvent(eventNo) {
    return Api().get('readEvent', {params: {eventNo: eventNo}})
  },
  updateEvent(eventNo, name, location, eventDate, ticketPrice) {
    return Api().post('updateEvent', {   
      eventNo: eventNo,    
      name: name,
      location: location,
      eventDate: eventDate,
      ticketPrice: ticketPrice
    })
  },
  deleteEvent(eventNo) {
    return Api().post('deleteEvent', {   
      eventNo: eventNo    
    })
  }
}