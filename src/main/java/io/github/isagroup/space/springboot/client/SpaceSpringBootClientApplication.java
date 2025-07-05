package io.github.isagroup.space.springboot.client;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

import io.github.isagroup.space.springboot.client.contracts.ContractsService;
import io.github.isagroup.space.springboot.client.contracts.CreateContractRequest;
import io.github.isagroup.space.springboot.client.contracts.UserContactRequest;
import io.github.isagroup.space.springboot.client.services.FallBackSubscription;
import io.github.isagroup.space.springboot.client.services.PricingAvailabilityStatus;
import io.github.isagroup.space.springboot.client.services.ServicesService;
import io.github.isagroup.space.springboot.client.users.NewUserRequest;
import io.github.isagroup.space.springboot.client.users.Role;
import io.github.isagroup.space.springboot.client.users.RoleRequest;
import io.github.isagroup.space.springboot.client.users.User;
import io.github.isagroup.space.springboot.client.users.UsersService;

@SpringBootApplication
public class SpaceSpringBootClientApplication {

	private static final Log logger = LogFactory.getLog(SpaceSpringBootClientApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpaceSpringBootClientApplication.class, args);
	}

	@Bean
	public CommandLineRunner run(UsersService usersService, ServicesService servicesService,
			ContractsService contractsService) {
		return run -> {
			testUserServiceMethods(usersService);
			testServicesMethods(servicesService);
			User admin = usersService.getUserByUsername("admin");

			testContractsMethods(contractsService, admin.id());
		};
	}

	private void testUserServiceMethods(UsersService usersService) {
		NewUserRequest newUser = new NewUserRequest("johndoe", "foobar", Role.EVALUATOR);

		logger.info(usersService.getUsers());
		logger.info(usersService.createUser(newUser));
		logger.info(usersService.getUserByUsername(newUser.username()));
		logger.info(usersService.updateUserApiKey(newUser.username()));
		logger.info(usersService.updateUserRole(newUser.username(), new RoleRequest(Role.ADMIN)));
		usersService.deleteUserByUsername(newUser.username());
	}

	private void testServicesMethods(ServicesService servicesService) {
		String petName = "petclinic";
		String serviceTest = "ikea";
		String alfaIkeaPath = "src/main/resources/ikea-alfa.yaml";
		String betaIkeaPath = "src/main/resources/ikea-beta.yaml";

		logger.info(servicesService.createService(new FileSystemResource("src/main/resources/petclinic.yaml")));
		logger.info(servicesService.createService(new FileSystemResource(alfaIkeaPath)));
		logger.info(servicesService.getServices(null, null));
		logger.info(servicesService.getServices(new SearchParams(new OffsetBasedPagination(0)), null));
		logger.info(servicesService.getServices(new SearchParams(new OffsetBasedPagination(0)), petName));
		logger.info(servicesService.getServices(new SearchParams(new PagedBasedPagination(1)), null));
		logger.info(servicesService.getServiceByName(petName));
		logger.info(servicesService.addPricingToService(serviceTest,
				new FileSystemResource(betaIkeaPath)));
		logger.info(servicesService.getPricingsDefinedInService(serviceTest, null));
		logger.info(servicesService.getPricingsDefinedInService(serviceTest, null));
		logger.info(servicesService.getServicePricingByVersion(serviceTest, "alfa"));
		logger.info(servicesService.updatePricingAvailabilityByVersion(serviceTest, "alfa",
				PricingAvailabilityStatus.ARCHIVED,
				new FallBackSubscription("FAMILY", null)));

		servicesService.deleteServiceByName(serviceTest);
		servicesService.deleteServiceByName(petName);

	}

	private void testContractsMethods(ContractsService contractsService, String userId) {
		logger.info(contractsService.getContracts(new SearchParams(new OffsetBasedPagination(0))));

		CreateContractRequest newContract = new CreateContractRequest(
				new UserContactRequest(userId, "admin", null, null, null, null),
				Map.of("ikea", "FAMILY"),
				Map.of());

		logger.info(contractsService.getContracts(new SearchParams(new OffsetBasedPagination(0))));
		logger.info(contractsService.createContract(newContract));

	}

}
