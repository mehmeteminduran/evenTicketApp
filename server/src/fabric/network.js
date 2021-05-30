
'use strict';

const { Gateway,Wallets } = require('fabric-network'); 
const EventStrategies = require('fabric-network/lib/impl/event/defaulteventhandlerstrategies');
const FabricCAServices = require('fabric-ca-client');
const path = require('path');
const { buildCAClient, registerAndEnrollUser, enrollAdmin } = require('../fabric/Utilities/CAUtil.js');
const { buildCCPOrg1, buildWallet } = require('../fabric/Utilities/AppUtil.js');
const fs = require('fs'); 


// capture network variables from config.json
const configPath = path.join(process.cwd(), '/config.json');
const configJSON = fs.readFileSync(configPath, 'utf8');
const config = JSON.parse(configJSON);
var connection_file = config.connection_file;
var userName = config.userName;
var gatewayDiscovery = config.gatewayDiscovery;
var caName = config.caName;
// connect to the connection file
const ccpPath = path.join(process.cwd(), connection_file);
const ccpJSON = fs.readFileSync(ccpPath, 'utf8');
const ccp = JSON.parse(ccpJSON);

const channelName = 'mychannel';
const chaincodeName_event = 'eventcont'; 
const chaincodeName_ticket = 'ticketcont';
const walletPath = path.resolve(__dirname, '..', '..','wallet','Org1');
 

async function initGatewayForOrg1(useCommitEvents) { 
	// build an in memory object with the network configuration (also known as a connection profile)
	const ccpOrg1 = buildCCPOrg1();

	// build an instance of the fabric ca services client based on
	// the information in the network configuration
	//const caOrg1Client = buildCAClient(FabricCAServices, ccpOrg1, caName);

	// setup the wallet to cache the credentials of the application user, on the app server locally
	const walletOrg1 = await buildWallet(Wallets, walletPath);
    console.log(`Wallet path: ${walletPath}`);

    // in a real application this would be done on an administrative flow, and only once
	// stores admin identity in local wallet, if needed
	//await enrollAdmin(caOrg1Client, walletOrg1, org1);
	// register & enroll application user with CA, which is used as client identify to make chaincode calls
	// and stores app user identity in local wallet
	// In a real application this would be done only when a new user was required to be added
	// and would be part of an administrative flow
	//await registerAndEnrollUser(caOrg1Client, walletOrg1, org1, userName, 'org1.department1');


	try {
		// Create a new gateway for connecting to Org's peer node.
		const gatewayOrg1 = new Gateway();

		if (useCommitEvents) {
			await gatewayOrg1.connect(ccpOrg1, {
				wallet: walletOrg1,
				identity: userName,
				discovery: gatewayDiscovery
			});
		} else {
			await gatewayOrg1.connect(ccpOrg1, {
				wallet: walletOrg1,
				identity: userName,
				discovery: gatewayDiscovery,
				eventHandlerOptions: EventStrategies.NONE
			});
		}


		return gatewayOrg1;
	} catch (error) {
		console.error(`Error in connecting to gateway for Org1: ${error}`);
		process.exit(1);
	}
}

// create event transaction
exports.createEvent = async function(eventNo, name, location, eventDate, ticketPrice) {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true); 

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_event);

        // Submit the specified transaction. 
        await contract.submitTransaction('CreateEvent', eventNo, name, location, eventDate, ticketPrice);
        console.log('Transaction has been submitted');

        // Disconnect from the gateway.
        await gateway.disconnect();

        response.msg = 'CreateEvent Transaction has been submitted';
        return response;        

    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`);
        response.error = error.message;
        return response; 
    }
}

// update event transaction
exports.updateEvent = async function(eventNo, name, location, eventDate, ticketPrice) {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true); 

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_event);

        // Submit the specified transaction. 
        await contract.submitTransaction('UpdateEvent', eventNo, name, location, eventDate, ticketPrice);
        console.log('Transaction has been submitted');

        // Disconnect from the gateway.
        await gateway.disconnect();

        response.msg = 'updateEvent Transaction has been submitted';
        return response;        

    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`);
        response.error = error.message;
        return response; 
    }
}
// create event transaction
exports.deleteEvent = async function(eventNo) {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true); 

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_event);

        // Submit the specified transaction. 
        await contract.submitTransaction('DeleteEvent', eventNo);
        console.log('Transaction has been submitted');

        // Disconnect from the gateway.
        await gateway.disconnect();

        response.msg = 'CreateEvent Transaction has been submitted';
        return response;        

    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`);
        response.error = error.message;
        return response; 
    }
}
// query all events transaction
exports.queryAllEvents = async function() {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true);  

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_event);

        // Evaluate the specified transaction. 
        const result = await contract.evaluateTransaction('GetAllEvents'); 
        console.log(`Transaction has been evaluated, result is: ${result.toString()}`);

        return result;

    } catch (error) {
        console.error(`Failed to evaluate transaction: ${error}`);
        response.error = error.message;
        return response;
    }
}

// query an event transaction
exports.readEvent = async function(eventNo) {
    try {
  
        var response = {};
  
        const gateway = await initGatewayForOrg1(true);  
  
        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);
  
        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_event);
  
        // Evaluate the specified transaction. 
        const result = await contract.evaluateTransaction('ReadEvent',eventNo); 
        console.log(`Transaction has been evaluated, result is: ${result.toString()}`);
  
        return result;
  
    } catch (error) {
        console.error(`Failed to evaluate transaction: ${error}`);
        response.error = error.message;
        return response;
    }
  }
  

// create event transaction
exports.createTicket = async function(ticketNo, eventNo, owner, appraisedValue) {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true); 

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_ticket);

        // Submit the specified transaction. 
        await contract.submitTransaction('CreateTicket',ticketNo, eventNo, owner, appraisedValue);
        console.log('Transaction has been submitted');

        // Disconnect from the gateway.
        await gateway.disconnect();

        response.msg = 'CreateTicket Transaction has been submitted';
        return response;        

    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`);
        response.error = error.message;
        return response; 
    }
}

// query all tickets transaction
exports.queryAllTickets = async function() {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true);  

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_ticket);

        // Evaluate the specified transaction. 
        const result = await contract.evaluateTransaction('GetAllTickets'); 
        console.log(`Transaction has been evaluated, result is: ${result.toString()}`);

        return result;

    } catch (error) {
        console.error(`Failed to evaluate transaction: ${error}`);
        response.error = error.message;
        return response;
    }
}

// query an ticket transaction
exports.readTicket = async function(ticketNo) {
    try {
  
        var response = {};
  
        const gateway = await initGatewayForOrg1(true);  
  
        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);
  
        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_ticket);
  
        // Evaluate the specified transaction. 
        const result = await contract.evaluateTransaction('ReadTicket',eventNo); 
        console.log(`Transaction has been evaluated, result is: ${result.toString()}`);
  
        return result;
  
    } catch (error) {
        console.error(`Failed to evaluate transaction: ${error}`);
        response.error = error.message;
        return response;
    }
  }

// change ticket owner transaction
exports.transferTicket = async function(ticketNo, newOwner, appraisedValue) {
    try {

        var response = {};

        const gateway = await initGatewayForOrg1(true);  

        // Get the network (channel) our contract is deployed to.
        const network = await gateway.getNetwork(channelName);

        // Get the contract from the network.
        const contract = network.getContract(chaincodeName_ticket);

        // Submit the specified transaction. 
        await contract.submitTransaction('TransferTicket', ticketNo, newOwner, appraisedValue);
        console.log('Transaction has been submitted');

        // Disconnect from the gateway.
        await gateway.disconnect();

        response.msg = 'TransferTicket Transaction has been submitted';
        return response;        

    } catch (error) {
        console.error(`Failed to submit transaction: ${error}`);
        response.error = error.message;
        return response; 
    }
}

