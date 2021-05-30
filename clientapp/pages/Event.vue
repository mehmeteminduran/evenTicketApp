<template>
  <div class="p-grid crud-demo">
    <div class="p-col-12">
      <div class="card">
        <Toast />
        <Toolbar class="p-mb-4">
          <template v-slot:left>
            <Button
              label="New Event"
              icon="pi pi-plus"
              class="p-button-success p-mr-2"
              @click="openNew"
            />
          </template>
          <template v-slot:right>
            <Button
              label="Export"
              icon="pi pi-upload"
              class="p-button-help"
              @click="exportCSV($event)"
            />
          </template>
        </Toolbar>

        <DataTable
          ref="dt"
          :value="events"
          dataKey="eventNo"
          :paginator="true"
          :rows="10" 
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
          :rowsPerPageOptions="[5, 10, 25]"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} events"
          responsiveLayout="scroll"
        >
          <template #header>
            <div
              class="table-header p-d-flex p-flex-column p-flex-md-row p-jc-md-between"
            >
              <h5 class="p-m-0">Manage Events</h5> 
            </div>
          </template>

          <Column field="eventNo" header="Event No" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">No</span>
              {{ slotProps.data.eventNo }}
            </template>
          </Column>
          <Column field="name" header="Name" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">Name</span>
              {{ slotProps.data.name }}
            </template>
          </Column>
          <Column field="location" header="Location" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">Location</span>
              {{ slotProps.data.location }}
            </template>
          </Column>
          <Column
            header="Date"
            filterField="date"
            dataType="date"
            style="min-width: 10rem"
          >
            <template #body="slotProps">
              <span class="p-column-title">Event Date</span>
              {{ formatDate(slotProps.data.eventDate) }}
            </template>
            <template #filter="{ filterModel }">
              <Calendar
                v-model="filterModel.value"
                dateFormat="mm/dd/yy"
                placeholder="mm/dd/yyyy"
              />
            </template>
          </Column>
          <Column field="ticketPrice" header="Ticket Price" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">Ticket Price</span>
              {{ formatCurrency(slotProps.data.ticketPrice) }}
            </template>
          </Column>
          <Column>
            <template #body="slotProps">
              <Button
                icon="pi pi-check"
                class="p-button-rounded p-button-success p-mr-2"
                @click="confirmBuyTicket(slotProps.data)"
              />
              <Button
                icon="pi pi-pencil"
                class="p-button-rounded p-button-warning p-mr-2"
                @click="editEvent(slotProps.data)"
              />
              <Button
                icon="pi pi-trash"
                class="p-button-rounded p-button-danger"
                @click="confirmDeleteEvent(slotProps.data)"
              />
            </template>
          </Column>
        </DataTable>

        <Dialog
          v-model:visible="eventDialog"
          :style="{ width: '450px' }"
          header="Event Details"
          :modal="true"
          class="p-fluid"
        >
          <div class="p-field">
            <label for="name">Name</label>
            <InputText
              id="name"
              v-model.trim="event.name"
              required="true"
              autofocus
              :class="{ 'p-invalid': submitted && !event.name }"
            />
            <small class="p-invalid" v-if="submitted && !event.name"
              >Name is required.</small
            >
          </div>
          <div class="p-field">
            <label for="location">Location</label>
            <InputText
              id="location"
              v-model.trim="event.location"
              required="false"
              autofocus
            />
          </div>
          <div class="p-field">
            <label for="eventDate">Date</label>
            <Calendar
              id="eventDate"
              v-model="event.eventDate"
              :showTime="false"
              :showIcon="true"
              :showButtonBar="true"
              appendTo="body"
            />
          </div>
          <div class="p-field p-col">
            <label for="ticketPrice">Price</label>
            <InputNumber
              id="ticketPrice"
              v-model="event.ticketPrice"
              integeronly
            />
          </div>
          <template #footer>
            <Button
              label="Cancel"
              icon="pi pi-times"
              class="p-button-text"
              @click="hideDialog"
            />
            <Button
              label="Save"
              icon="pi pi-check"
              class="p-button-text"
              @click="saveEvent"
            />
          </template>
        </Dialog>

        <Dialog
          v-model:visible="deleteEventDialog"
          :style="{ width: '450px' }"
          header="Delete an Event"
          :modal="true"
        >
          <div class="confirmation-content">
            <i
              class="pi pi-exclamation-triangle p-mr-3"
              style="font-size: 2rem"
            />
            <span v-if="event"
              >Are you sure you want to delete <b>{{ event.name }}</b
              >?</span
            >
          </div>
          <template #footer>
            <Button
              label="No"
              icon="pi pi-times"
              class="p-button-text"
              @click="deleteEventDialog = false"
            />
            <Button
              label="Yes"
              icon="pi pi-check"
              class="p-button-text"
              @click="deleteEvent"
            />
          </template>
        </Dialog>

        <Dialog
          v-model:visible="buyTicketDialog"
          :style="{ width: '450px' }"
          header="Buy a Ticket"
          :modal="true"
        >
          <div class="confirmation-content">
            <i
              class="pi pi-exclamation-triangle p-mr-3"
              style="font-size: 2rem"
            />
            <span v-if="event"
              >Are you sure you want to buy a ticket for
              <b>{{ event.name }}</b> event?</span
            >
          </div>
          <template #footer>
            <Button
              label="No"
              icon="pi pi-times"
              class="p-button-text"
              @click="buyTicketDialog = false"
            />
            <Button
              label="Yes"
              icon="pi pi-check"
              class="p-button-text"
              @click="buyTicket"
            />
          </template>
        </Dialog>
      </div>
    </div>
  </div>
</template>

<script>
import EventService from "@/api/apiService";

export default {
  data() {
    return {
      events: null,
      eventDialog: false,
      deleteEventDialog: false,
      buyTicketDialog: false,
      event: {},
      submitted: false,
    };
  },
  eventService: null,
  created() {},
  async mounted() {
    const apiResponse = await EventService.queryAllEvents();
	this.events = apiResponse.data;
  },
  methods: {
    formatCurrency(value) {
      if (value)
        return value.toLocaleString("en-US", {
          style: "currency",
          currency: "USD",
        });
      return;
    },
    formatDate(value) {
      if (value) {
        return new Date(value).toLocaleDateString("en-US", {
          day: "2-digit",
          month: "2-digit",
          year: "numeric",
        });
      }

      return "";
    },
    openNew() {
      this.event = {};
      this.submitted = false;
      this.eventDialog = true;
    },
    hideDialog() {
      this.eventDialog = false;
      this.submitted = false;
    },
    async saveEvent() {
      this.submitted = true;
      if (this.event.name.trim()) {
        if (this.event.eventNo) {

          var date = new Date(this.event.eventDate).toLocaleDateString(
            "en-US",
            {
              day: "2-digit",
              month: "2-digit",
              year: "numeric",
            }
          );
          const apiResponse = await EventService.updateEvent(
            this.event.eventNo,
            this.event.name,
            this.event.location,
            date,
            this.event.ticketPrice
          );
          if (!apiResponse.data.error) {
            const apiResponse = await EventService.queryAllEvents();
            this.events = apiResponse.data;
            this.$toast.add({
              severity: "success",
              summary: "Successful",
              detail: "Event Created",
              life: 3000,
            });
          } else {
            this.events[this.findIndexById(this.event.eventNo)] = this.event;
            this.$toast.add({
              severity: "error",
              summary: "Error",
              detail: apiResponse.data.error,
              life: 3000,
            });
          }
        } else {
          var eventDate = new Date(this.event.eventDate).toLocaleDateString(
            "en-US",
            {
              day: "2-digit",
              month: "2-digit",
              year: "numeric",
            }
          );
          const apiResponse = await EventService.createEvent(
            this.event.name,
            this.event.location,
            eventDate,
            this.event.ticketPrice
          );
          if (!apiResponse.data.error) {
            // this.response = apiResponse.data;
            // this.event.eventNo = apiResponse.data.eventNo;
            // this.events.push(this.event);
            const apiResponse = await EventService.queryAllEvents();
            this.events = apiResponse.data;
            this.$toast.add({
              severity: "success",
              summary: "Successful",
              detail: "Event Created",
              life: 3000,
            });
          } else {
            this.$toast.add({
              severity: "error",
              summary: "Error",
              detail: apiResponse.data.error,
              life: 3000,
            });
          }
        }
        this.eventDialog = false;
        this.event = {};
      }
    },
    editEvent(event) {
      this.event = { ...event };
      this.eventDialog = true;
    },
    confirmDeleteEvent(event) {
      this.event = event;
      this.deleteEventDialog = true;
    },
    confirmBuyTicket(event) {
      this.event = event;
      this.buyTicketDialog = true;
    },
    async deleteEvent() {
      const apiResponse = await EventService.deleteEvent(this.event.eventNo);
      this.response = apiResponse.data;
      this.events = this.events.filter(
        (val) => val.eventNo !== this.event.eventNo
      );
      this.deleteEventDialog = false;
      this.event = {};
      this.$toast.add({
        severity: "success",
        summary: "Successful",
        detail: "Event Deleted",
        life: 3000,
      });
    },
    findIndexById(id) {
      let index = -1;
      for (let i = 0; i < this.events.length; i++) {
        if (this.events[i].eventNo === id) {
          index = i;
          break;
        }
      }
      return index;
    },
    async buyTicket() {
      this.submitted = true;
      const apiResponse = await EventService.createTicket(
        this.event.eventNo,
        "11111111110",
        this.event.ticketPrice
      );
      this.response = apiResponse.data;
      this.$toast.add({
        severity: "success",
        summary: "Successful",
        detail: "Ticket Bought",
        life: 3000,
      });

      this.buyTicketDialog = false;
      this.event = {};
    },
    exportCSV() {
      this.$refs.dt.exportCSV();
    },
    confirmDeleteSelected() {
      this.deleteEventsDialog = true;
    },
  },
};
</script>

<style scoped lang="scss">
.table-header {
  display: flex;
  justify-content: space-between;
}

.event-image {
  width: 100px;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23);
}

.p-dialog .event-image {
  width: 150px;
  margin: 0 auto 2rem auto;
  display: block;
}

.confirmation-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.event-badge {
  border-radius: 2px;
  padding: 0.25em 0.5rem;
  text-transform: uppercase;
  font-weight: 700;
  font-size: 12px;
  letter-spacing: 0.3px;

  &.status-instock {
    background: #c8e6c9;
    color: #256029;
  }

  &.status-outofstock {
    background: #ffcdd2;
    color: #c63737;
  }

  &.status-lowstock {
    background: #feedaf;
    color: #8a5340;
  }
}

::v-deep(.p-toolbar) {
  flex-wrap: wrap;
  .p-button {
    margin-bottom: 0.25rem;
  }
}
</style>
