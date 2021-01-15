package br.com.dhecastro;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachine;

import br.com.dhecastro.enums.AccountEvents;
import br.com.dhecastro.enums.AccountStates;

@SpringBootApplication
public class AccountStateMachineApplication implements CommandLineRunner {

	@Autowired
	private StateMachine<AccountStates, AccountEvents> stateMachine;

	public static void main(String[] args) {
		SpringApplication.run(AccountStateMachineApplication.class, args);
	}

	@Override
	public void run(String... args) {
		System.out.println("Iniciando máquina de estados...");
		stateMachine.sendEvent(AccountEvents.ACCOUNT_SUBMITTED);
		stateMachine.sendEvent(new Message<AccountEvents>() {
			@Override
			public AccountEvents getPayload() {
				return AccountEvents.ACCOUNT_ANALYZED;
			}

			@Override
			public MessageHeaders getHeaders() {
				final Map<String, Object> params = new HashMap<>();
				//Se setar uma data que não seja dia útil, a máquina de estados
				//vai apenas até o estado de análise, por conta da
				//condição de guarda
				//Modifique a data abaixo para dia 9 (sábado), execute
				//e verifique a saída no console
				final LocalDate saturday = LocalDate.of(2021, 1, 8);
				params.put("day", saturday);
				return new MessageHeaders(params);
			}
		});
		stateMachine.sendEvent(AccountEvents.ACCOUNT_CONFIRMED);
		stateMachine.sendEvent(AccountEvents.ACCOUNT_CLOSED);
		System.out.println("Máquina de estados finalizada");
	}
}
