package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.Party;
import net.corda.core.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class MetalContract implements Contract {

    public static final String CID = "com.template.contracts.MetalContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) throws IllegalArgumentException {
        if(tx.getCommands().size() != 1 ) throw new IllegalArgumentException("the transaction must have one command");

        Command command = tx.getCommand(0);
        CommandData commandtype = command.getValue();
        List<PublicKey> requiredsigners = command.getSigners();

        //------------------------------------------issue command rules-----------------------------------------

        if(commandtype instanceof issue){

            //shape rules

            if(tx.getInputs().size() != 0) throw new IllegalArgumentException("the issue command connot have input");

            if(tx.getOutputs().size() != 1) throw new IllegalArgumentException("the issue can only have one output");

            //content rules

            ContractState outputstate = tx.getOutput(0);

            if(!(outputstate instanceof MetalState)) throw new IllegalArgumentException("output must be a metal state");

            MetalState metalState = (MetalState) outputstate;

            if(!metalState.getMetalName().equals("Gold")) throw new IllegalArgumentException("Metal is not gold");

            //signer rules

            Party issuer = metalState.getIssuer();
            PublicKey issuerkey = issuer.getOwningKey();

            if(!requiredsigners.contains(issuerkey)) throw new IllegalArgumentException("issuer has to sign the issuance");


        }
        else if(commandtype instanceof transfer){

            //shape rules
            if(tx.getInputs().size() != 1) throw new IllegalArgumentException("the transfer command can only have one input");

            if(tx.getOutputs().size() != 1) throw new IllegalArgumentException("the transfer command can only have one output");

            //content rules

            ContractState outputstate = tx.getOutput(0);
            ContractState inputstate = tx.getInput(0);

            if(!(outputstate instanceof MetalState)) throw new IllegalArgumentException("output must be a metal state");

            MetalState metalState = (MetalState) inputstate;

            if(!metalState.getMetalName().equals("Gold")) throw new IllegalArgumentException("Metal is not gold");

            //signer rules

            Party owner = metalState.getOwner();
            PublicKey ownerkey = owner.getOwningKey();

            if(!requiredsigners.contains(ownerkey)) throw new IllegalArgumentException("owner has to sign the transfer");


        }
        else throw new IllegalArgumentException(("unrecognized command"));
    }


    public static class issue implements CommandData{}
    public static class transfer implements CommandData{}


}