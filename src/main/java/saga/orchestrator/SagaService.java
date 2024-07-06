package saga.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SagaService {

    @Autowired
    private RestTemplate restTemplate;

    public void createOrderSaga() {
        try {
            String orderResponse = restTemplate.postForObject("http://localhost:8081/orders/create", null, String.class);
            System.out.println(orderResponse);

            String paymentResponse = restTemplate.postForObject("http://localhost:8082/payments/process", null, String.class);
            System.out.println(paymentResponse);

            String inventoryResponse = restTemplate.postForObject("http://localhost:8083/inventory/reserve", null, String.class);
            System.out.println(inventoryResponse);

        } catch (Exception e) {
            System.out.println("Transaction failed, compensating...");
            compensateOrderSaga();
        }
    }

    private void compensateOrderSaga() {
        try {
            String paymentCompensationResponse = restTemplate.postForObject("http://localhost:8082/payments/compensate", null, String.class);
            System.out.println(paymentCompensationResponse);
        } catch (Exception e) {
            System.out.println("Payment compensation failed");
        }

        try {
            String inventoryCompensationResponse = restTemplate.postForObject("http://localhost:8083/inventory/compensate", null, String.class);
            System.out.println(inventoryCompensationResponse);
        } catch (Exception e) {
            System.out.println("Inventory compensation failed");
        }

        try {
            String orderCompensationResponse = restTemplate.postForObject("http://localhost:8081/orders/compensate", null, String.class);
            System.out.println(orderCompensationResponse);
        } catch (Exception e) {
            System.out.println("Order compensation failed");
        }
    }
}