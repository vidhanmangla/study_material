package com.template.contracts;

import com.template.states.MetalState;
import com.template.states.TemplateState;
import net.corda.core.cordapp.Cordapp;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.core.DummyCommandData;
import net.corda.testing.core.TestIdentity;
import net.corda.testing.node.MockServices;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;

import java.util.Arrays;

import static net.corda.testing.node.NodeTestUtils.ledger;
import static net.corda.testing.node.NodeTestUtils.transaction;


public class ContractTests {

    private final TestIdentity Mint = new TestIdentity(new CordaX500Name("mint", "", "GB"));
    private final TestIdentity TraderA = new TestIdentity(new CordaX500Name("traderA", "", "US"));
    private final TestIdentity TraderB = new TestIdentity(new CordaX500Name("traderB", "", "US"));

    private final MockServices ledgerServices = new MockServices();

    private MetalState metalState = new MetalState("Gold", 10, Mint.getParty(), TraderA.getParty());
    private MetalState metalStateInput = new MetalState("Gold", 10, Mint.getParty(), TraderA.getParty());
    private MetalState metalStateOutput = new MetalState("Gold", 10, Mint.getParty(), TraderB.getParty());

    public void metalContractImplementsContracts() {
        assert (new MetalContract() instanceof Contract);
    }

    //--------------------issue command test cases --------------------------------------------//

    @Test
    public void metalContractRequiresZeroInputsInTheTransaction() {

        transaction(ledgerServices, tx -> {

            tx.input(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;

        });

    }

    @Test
    public void metalContractRequiresOneOutputInIssueTransaction() {

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;

        });

    }

    @Test
    public void metalContractRequiresTheTransactionToBeAMetalState() {

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, new DummyState());
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;

        });

    }

    @Test
    public void metalContractRequiresTheTransactionCommandToBeAnIssueCommand() {

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;

        });

    }

    @Test
    public void metalContractRequiresTheIssuerToBeARequiredSignerInTheTransaction() {

        transaction(ledgerServices, tx -> {
            tx.output(MetalContract.CID, metalState);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.output(MetalContract.CID, metalState);
            tx.command(Mint.getPublicKey(), new MetalContract.issue());
            tx.verifies();
            return null;

        });

    }

    //------------------------transfer command test cases---------------------------------------
    @Test
    public void metalContractRequiresOneInputAndOneOutputInTheTransaction() {

        transaction(ledgerServices, tx -> {

            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.verifies();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.input(MetalContract.CID, metalStateInput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.fails();
            return null;

        });

    }

    @Test
    public void metalContractRequiresTheTransactionCommandToBeATransaferCommand() {

        transaction(ledgerServices, tx -> {

            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), DummyCommandData.INSTANCE);
            tx.fails();
            return null;

        });

        transaction(ledgerServices, tx -> {

            tx.input(MetalContract.CID, metalStateInput);
            tx.output(MetalContract.CID, metalStateOutput);
            tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
            tx.verifies();
            return null;

        });
    }


        @Test
        public void metalContractRequiresTheOwnerToBeARequiredSigner(){

            transaction(ledgerServices, tx -> {

                tx.input(MetalContract.CID, metalStateInput);
                tx.output(MetalContract.CID, metalStateOutput);
                tx.command(TraderA.getPublicKey(), new MetalContract.transfer());
                tx.verifies();
                return null;

            });

            transaction(ledgerServices, tx -> {

                tx.input(MetalContract.CID, metalStateInput);
                tx.output(MetalContract.CID, metalStateOutput);
                tx.command(Mint.getPublicKey(), new MetalContract.transfer());
                tx.fails();
                return null;

            });


        }
    }

