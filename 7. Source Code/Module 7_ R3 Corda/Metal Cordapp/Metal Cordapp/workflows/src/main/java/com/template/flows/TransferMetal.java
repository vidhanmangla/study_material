package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

import static net.corda.core.utilities.ProgressTracker.Step;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class TransferMetal extends FlowLogic<SignedTransaction> {

    //declare variables and initialize them into constructor
    private String metalName;
    private int weight;
    private Party newOwner;
    private int input = 0;

    public TransferMetal(String metalName, int weight, Party owner) {
        this.metalName = metalName;
        this.weight = weight;
        this.newOwner = newOwner;
    }


    //creating the steps
    private final Step RETRIEVING_NOTARY = new Step("Retrieving the Notary.");
    private final Step GENERATING_TRANSACTION = new Step("Generating transaction.");
    private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
    private final Step COUNTERPARTY_SESSION = new Step("Sending flow to counterparty.");
    private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction");



    private final ProgressTracker progressTracker = new ProgressTracker(
            //steps
            RETRIEVING_NOTARY,
            GENERATING_TRANSACTION,
            SIGNING_TRANSACTION,
            COUNTERPARTY_SESSION,
            FINALISING_TRANSACTION

    );


    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    StateAndRef<MetalState> checkForMetalStates() throws FlowException{

        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);

        List<StateAndRef<MetalState>> Metalstates = getServiceHub().getVaultService().queryBy(MetalState.class, generalCriteria).getStates();

        boolean inputfound = false;
        int t = Metalstates.size();

        for(int x = 0; x < t; x++){
            if(Metalstates.get(x).getState().getData().getMetalName().equals(metalName)
                && Metalstates.get(x).getState().getData().getWeight() == weight){

                input = x;
                inputfound = true;

            }
        }
        if(inputfound){
            System.out.println("input found");
        }else{
            System.out.println("input not found");
            throw new FlowException();
        }
        return Metalstates.get(input);



    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        //retrieve notary identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party Notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

        StateAndRef<MetalState> inputstate = null;

        inputstate = checkForMetalStates();

        Party issuer = inputstate.getState().getData().getIssuer();


        //create transaction component
        MetalState outputstate = new MetalState(metalName,weight,issuer,newOwner);
        Command cmd = new Command(new MetalContract.transfer(), getOurIdentity().getOwningKey());


        //create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txb = new TransactionBuilder(Notary)
                .addOutputState(outputstate,MetalContract.CID)
                .addCommand(cmd);

        txb.addInputState(inputstate);



        //sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedtx = getServiceHub().signInitialTransaction(txb);



        //create session counterparty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(newOwner);
        FlowSession mintPartySession = initiateFlow(issuer);


        //finalize the transaction
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow( signedtx, otherPartySession, mintPartySession));

    }
}
