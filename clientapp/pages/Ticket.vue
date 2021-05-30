<template>
  <div class="p-grid crud-demo">
    <div class="p-col-12">
      <div class="card">
        <Toast />
        <Toolbar class="p-mb-4">
          <template v-slot:right>
            <Button
              label="Export"
              icon="pi pi-upload"
              class="p-button-help"
              @click="exportCSV($ticket)"
            />
          </template>
        </Toolbar>

        <DataTable
          ref="dt"
          :value="tickets"
          dataKey="ticketNo"
          :paginator="true"
          :rows="10"
          paginatorTemplate="FirstPageLink PrevPageLink PageLinks NextPageLink LastPageLink CurrentPageReport RowsPerPageDropdown"
          :rowsPerPageOptions="[5, 10, 25]"
          currentPageReportTemplate="Showing {first} to {last} of {totalRecords} tickets"
          responsiveLayout="scroll"
        >
          <template #header>
            <div
              class="table-header p-d-flex p-flex-column p-flex-md-row p-jc-md-between"
            >
              <h5 class="p-m-0">Manage Tickets</h5>
            </div>
          </template>
          <Column field="ticketNo" header="Ticket No" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">No</span>
              {{ slotProps.data.ticketNo }}
            </template>
          </Column>
          <Column field="eventNo" header="Event No" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">Event No</span>
              {{ slotProps.data.eventNo }}
            </template>
          </Column>
          <Column field="owner" header="Owner" :sortable="true">
            <template #body="slotProps">
              <span class="p-column-title">Owner</span>
              {{ slotProps.data.owner }}
            </template>
          </Column>
          <Column
            field="appraisedValue"
            header="Appraised Price"
            :sortable="true"
          >
            <template #body="slotProps">
              <span class="p-column-title">Appraised Price</span>
              {{ formatCurrency(slotProps.data.appraisedValue) }}
            </template>
          </Column>
          <Column>
            <template #body="slotProps">
              <Button
                icon="pi pi-send"
                class="p-button-rounded p-button-success p-mr-2"
                @click="transferTicket(slotProps.data)"
              />
            </template>
          </Column>
        </DataTable>

        <Dialog
          v-model:visible="ticketTransferDialog"
          :style="{ width: '450px' }"
          header="Transfer Ticket"
          :modal="true"
          class="p-fluid"
        >
          <div class="p-field">
            <label for="name">New Owner</label>
            <InputText
              id="newOwner"
              v-model.trim="ticket.newOwner"
              required="true"
              autofocus
              :class="{ 'p-invalid': submitted && !ticket.newOwner }"
            />
            <small class="p-invalid" v-if="submitted && !ticket.newOwner"
              >New Owner is required.</small
            >
          </div>
          <div class="p-field p-col">
            <label for="price">Price</label>
            <InputNumber id="price" v-model="ticket.ticketPrice" integeronly />
          </div>
          <template #footer>
            <Button
              label="Cancel"
              icon="pi pi-times"
              class="p-button-text"
              @click="hideDialog"
            />
            <Button
              label="Send"
              icon="pi pi-check"
              class="p-button-text"
              @click="saveTicket"
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
      tickets: null,
      ticketTransferDialog: false,
      ticket: {},
      submitted: false,
    };
  },
  eventService: null,
  created() {},
  async mounted() {
    const apiResponse = await EventService.queryAllTickets();
    this.tickets = apiResponse.data;
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
    hideDialog() {
      this.ticketTransferDialog = false;
      this.submitted = false;
    },
    async saveTicket() {
      this.submitted = true;

      if (this.ticket.ticketNo) {
        const apiResponse = await EventService.transferTicket(
          this.ticket.ticketNo,
          this.ticket.newOwner,
          this.ticket.ticketPrice
        );
        if (!apiResponse.data.error) {
          const apiResponse = await EventService.queryAllTickets();
          this.tickets = apiResponse.data;
          this.$toast.add({
            severity: "success",
            summary: "Successful",
            detail: "Event Created",
            life: 3000,
          });
        } else {
          this.tickets[this.findIndexById(this.ticket.ticketNo)] = this.ticket;
          this.$toast.add({
            severity: "error",
            summary: "Error",
            detail: apiResponse.data.error,
            life: 3000,
          });
        }

        this.ticketTransferDialog = false;
        this.ticket = {};
      }
    },
    transferTicket(ticket) {
      this.ticket = { ...ticket };
      this.ticketTransferDialog = true;
    },
    findIndexById(id) {
      let index = -1;
      for (let i = 0; i < this.tickets.length; i++) {
        if (this.tickets[i].ticketNo === id) {
          index = i;
          break;
        }
      }
      return index;
    },
    exportCSV() {
      this.$refs.dt.exportCSV();
    },
  },
};
</script>

<style scoped lang="scss">
.table-header {
  display: flex;
  justify-content: space-between;
}

.ticket-image {
  width: 100px;
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23);
}

.p-dialog .ticket-image {
  width: 150px;
  margin: 0 auto 2rem auto;
  display: block;
}

.confirmation-content {
  display: flex;
  align-items: center;
  justify-content: center;
}

.ticket-badge {
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
