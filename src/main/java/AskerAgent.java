import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;

import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class AskerAgent extends Agent {
    private final String city = "Torino,Italy";

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            int nResponders = args.length;

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

            for (Object arg : args) {
                cfp.addReceiver(new AID((String) arg, AID.ISLOCALNAME));
            }
            cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            cfp.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            cfp.setContent(city);

            addBehaviour(new MyContractNetInitiator(this, cfp, nResponders));
        } else {
            System.out.println("Responders non specificati.");
            doDelete();
        }
    }


    private static class MyContractNetInitiator extends ContractNetInitiator {
        private int nResponders;

        public MyContractNetInitiator(Agent a, ACLMessage cfp, int nResponders) {
            super(a, cfp);
            this.nResponders = nResponders;
        }

        protected void agentLog(String msg) {
            System.out.println(myAgent.getLocalName() + ": " + msg);
        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            agentLog("L'agente " + propose.getSender().getLocalName() + " ha proposto " + propose.getContent());
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            agentLog("L'agente " + refuse.getSender().getLocalName() + " ha rifiutato la richiesta: " + refuse.getContent());
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                agentLog("Il Responder specificato non esiste");
            } else {
                agentLog("L'agente " + failure.getSender().getLocalName() + " ha fallito");
            }
            nResponders--;
        }

        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() < nResponders) {
                agentLog("Timeout: mancano " + (nResponders - responses.size()) + " risposte");
            }
            // Evaluate proposals.
            double bestProposal = Double.MAX_VALUE;
            AID bestProposer = null;
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    double proposal = Double.parseDouble(msg.getContent());
                    if (proposal < bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                }
            }
            // Accept the proposal of the best proposer
            if (accept != null) {
                agentLog("Sto accettando " + bestProposal + " dall'agente " + bestProposer.getLocalName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            agentLog("Ho effettuato con successo la richiesta delle previsioni del tempo");
        }

    }
}
