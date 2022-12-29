package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.MetalContract;
import com.template.states.MetalState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.Vault.StateStatus;
import net.corda.core.node.services.vault.IQueryCriteriaParser;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import org.jetbrains.annotations.NotNull;

import javax.persistence.criteria.Predicate;
import java.util.Collection;

import static net.corda.core.utilities.ProgressTracker.*;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class IssueMetal extends FlowLogic<SignedTransaction> {

    //declare variables and initialize them into constructor
    private String metalName;
    private int weight;
    private Party owner;

    public IssueMetal(String metalName, int weight, Party owner) {
        this.metalName = metalName;
        this.weight = weight;
        this.owner = owner;
    }


    //creating the steps
    private final ProgressTracker.Step RETRIEVING_NOTARY = new ProgressTracker.Step("Retrieving the Notary.");
    private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction.");
    private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
    private final ProgressTracker.Step COUNTERPARTY_SESSION = new ProgressTracker.Step("Sending flow to counterparty.");
    private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction");



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

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // Initiator flow logic goes here.

        //retrieve notary identity
        progressTracker.setCurrentStep(RETRIEVING_NOTARY);
        Party Notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);


        //create transaction component
        MetalState outputstate = new MetalState(metalName,weight,getOurIdentity(),owner);
        Command cmd = new Command(new MetalContract.issue(), getOurIdentity().getOwningKey());


        //create transaction builder
        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        TransactionBuilder txb = new TransactionBuilder(Notary)
                .addOutputState(outputstate,MetalContract.CID)
                .addCommand(cmd);



        //sign the transaction
        progressTracker.setCurrentStep(SIGNING_TRANSACTION);
        SignedTransaction signedtx = getServiceHub().signInitialTransaction(txb);



        //create session counterparty
        progressTracker.setCurrentStep(COUNTERPARTY_SESSION);
        FlowSession otherPartySession = initiateFlow(owner);


        //finalize the transaction
        progressTracker.setCurrentStep(FINALISING_TRANSACTION);
        return subFlow(new FinalityFlow( signedtx, otherPartySession));

    }
}
