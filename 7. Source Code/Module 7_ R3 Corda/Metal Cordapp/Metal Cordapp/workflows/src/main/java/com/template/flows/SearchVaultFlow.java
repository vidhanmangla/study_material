package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.states.MetalState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatingFlow;
import net.corda.core.flows.StartableByRPC;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.utilities.ProgressTracker;

import java.util.List;

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
public class SearchVaultFlow extends FlowLogic<Void> {

    void searchForAllStates(){

        QueryCriteria consumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.CONSUMED);
        List<StateAndRef<MetalState>> consumedMetalstates = getServiceHub().getVaultService().queryBy(MetalState.class, consumedCriteria).getStates();

        if(consumedMetalstates.size() < 1){
            System.out.println("no consumed metal states found");
        }else{
            System.out.println("consumed metal states found" + consumedMetalstates.size());
        }

        int c = consumedMetalstates.size();
        for(int i = 0; i<c; i++){
            System.out.println("name :" + consumedMetalstates.get(i).getState().getData().getMetalName());
            System.out.println("owner :" + consumedMetalstates.get(i).getState().getData().getOwner());
            System.out.println("weight :" + consumedMetalstates.get(i).getState().getData().getWeight());
            System.out.println("issuer :" + consumedMetalstates.get(i).getState().getData().getIssuer());
        }

        QueryCriteria unconsumedCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        List<StateAndRef<MetalState>> unconsumedMetalstates = getServiceHub().getVaultService().queryBy(MetalState.class, unconsumedCriteria).getStates();

        if(unconsumedMetalstates.size() < 1){
            System.out.println("no consumed metal states found");
        }else{
            System.out.println("consumed metal states found" + unconsumedMetalstates.size());
        }

        int u = unconsumedMetalstates.size();
        for(int i = 0; i<c; i++){
            System.out.println("name :" + unconsumedMetalstates.get(i).getState().getData().getMetalName());
            System.out.println("owner :" + unconsumedMetalstates.get(i).getState().getData().getOwner());
            System.out.println("weight :" + unconsumedMetalstates.get(i).getState().getData().getWeight());
            System.out.println("issuer :" + unconsumedMetalstates.get(i).getState().getData().getIssuer());
        }


    }



    @Suspendable
    @Override
    public Void call() throws FlowException {
        // Initiator flow logic goes here.

        searchForAllStates();

        return null;
    }
}
