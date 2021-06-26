import jade.core.Agent;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import jade.domain.FIPANames;

import java.util.Vector;

public class ContractNetInitiatorAgent extends Agent {
    private final String city = "Illinois";

    @Override
    protected void setup() {
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            String responderAgent = (String) args[0];
            System.out.println("Chiedo le previsioni per " + city);

            ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
            cfp.addReceiver(new AID(responderAgent, AID.ISLOCALNAME));
            cfp.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            cfp.setConversationId("weather-req");
            cfp.setContent(city);

            addBehaviour(new MyContractNetInitiator(this, cfp));
        } else {
            System.out.println("Responder non specificato.");
        }
    }


    private class MyContractNetInitiator extends ContractNetInitiator {

        public MyContractNetInitiator(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            System.out.println("L'agente " + propose.getSender().getLocalName() + " ha risposto con le seguenti previsioni del tempo:\n" + propose.getContent());
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("L'agente " + refuse.getSender().getLocalName() + " ha rifiutato la richiesta");
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                System.out.println("Il Responder specificato non esiste");
            } else {
                System.out.println("L'agente " + failure.getSender().getLocalName() + " ha fallito");
            }
        }
        
        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Ho effettuato con successo la richiesta delle previsioni del tempo per la città" + inform.getContent());
        }

    }
}
