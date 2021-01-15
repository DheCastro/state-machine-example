package br.com.dhecastro.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.EnumStateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.StateMachineConfigurationConfigurer;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.springframework.statemachine.guard.Guard;
import org.springframework.statemachine.listener.StateMachineListener;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

import br.com.dhecastro.enums.AccountEvents;
import br.com.dhecastro.enums.AccountStates;

@Configuration
@EnableStateMachine
public class AccountStateMachineTransitionByEventConfig
		extends EnumStateMachineConfigurerAdapter<AccountStates, AccountEvents> {

	@Override
	public void configure(StateMachineConfigurationConfigurer<AccountStates, AccountEvents> config) throws Exception {
		config.withConfiguration().autoStartup(true).listener(listener());
	}

	@Override
	public void configure(StateMachineStateConfigurer<AccountStates, AccountEvents> states) throws Exception {
		states
        .withStates()
        .initial(AccountStates.CREATED)
        .state(AccountStates.CREATED)
        .state(AccountStates.ANALYSIS)
        //Exemplo de ação na entrada do estado
        //E apesar de nula, também pode ser configurada uma ação de saída
        .state(AccountStates.APPROVED, sendEmailApprovedAccount(), null)
        .state(AccountStates.OPEN)
        .state(AccountStates.CLOSE);
	}

	@Override
	public void configure(StateMachineTransitionConfigurer<AccountStates, AccountEvents> transitions) throws Exception {
		transitions.withExternal()
				.source(AccountStates.CREATED).target(AccountStates.ANALYSIS)
				.event(AccountEvents.ACCOUNT_SUBMITTED)
				.and().withExternal()
				.source(AccountStates.ANALYSIS).target(AccountStates.APPROVED)
	               .event(AccountEvents.ACCOUNT_ANALYZED)
	               .guard(onlyWorkingDays())
	           .and().withExternal()
				.source(AccountStates.APPROVED).target(AccountStates.OPEN)
				.event(AccountEvents.ACCOUNT_CONFIRMED)
				.and().withExternal()
				.source(AccountStates.OPEN).target(AccountStates.CLOSE)
				.event(AccountEvents.ACCOUNT_CLOSED);
	}
	
	@Bean
	public Action<AccountStates, AccountEvents> sendEmailApprovedAccount() {
	   return context -> System.out.println("Email para informar que a conta foi aprovada");
	}
	
	@Bean
	public Guard<AccountStates, AccountEvents> onlyWorkingDays() {
	   return context -> !EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY).contains(
	           ((LocalDate) context.getMessage().getHeaders().get("day")).getDayOfWeek());
	}
	
	@Bean
	public StateMachineListener<AccountStates, AccountEvents> listener() {
		return new StateMachineListenerAdapter<AccountStates, AccountEvents>() {
			public void stateChanged(State<AccountStates, AccountEvents> from, State<AccountStates, AccountEvents> to) {
				if(from != null && to != null) {
					System.out.println("AccountStates change from " + from.getId() + " to " + to.getId());
				}
			}
		};
	}
}
