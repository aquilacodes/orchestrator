package saga.orchestrator;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.*;

@SpringBootTest
public class SagaServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private SagaService sagaService;

    public SagaServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateOrderSaga_Success() {
        when(restTemplate.postForObject("http://localhost:8081/orders/create", null, String.class)).thenReturn("Order created");
        when(restTemplate.postForObject("http://localhost:8082/payments/process", null, String.class)).thenReturn("Payment processed");
        when(restTemplate.postForObject("http://localhost:8083/inventory/reserve", null, String.class)).thenReturn("Inventory reserved");

        sagaService.createOrderSaga();

        verify(restTemplate, times(1)).postForObject("http://localhost:8081/orders/create", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8082/payments/process", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8083/inventory/reserve", null, String.class);
    }

    @Test
    public void testCreateOrderSaga_FailureAndCompensation() {
        when(restTemplate.postForObject("http://localhost:8081/orders/create", null, String.class)).thenReturn("Order created");
        when(restTemplate.postForObject("http://localhost:8082/payments/process", null, String.class)).thenReturn("Payment processed");
        when(restTemplate.postForObject("http://localhost:8083/inventory/reserve", null, String.class)).thenThrow(new RuntimeException("Inventory reservation failed"));

        sagaService.createOrderSaga();

        verify(restTemplate, times(1)).postForObject("http://localhost:8081/orders/create", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8082/payments/process", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8083/inventory/reserve", null, String.class);

        verify(restTemplate, times(1)).postForObject("http://localhost:8082/payments/compensate", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8083/inventory/compensate", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8081/orders/compensate", null, String.class);
    }

    @Test
    public void testCreateOrderSaga_PaymentFailure() {
        when(restTemplate.postForObject("http://localhost:8081/orders/create", null, String.class)).thenReturn("Order created");
        when(restTemplate.postForObject("http://localhost:8082/payments/process", null, String.class)).thenThrow(new RuntimeException("Payment processing failed"));

        sagaService.createOrderSaga();

        verify(restTemplate, times(1)).postForObject("http://localhost:8081/orders/create", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8082/payments/process", null, String.class);

        verify(restTemplate, times(1)).postForObject("http://localhost:8082/payments/compensate", null, String.class);
        verify(restTemplate, times(1)).postForObject("http://localhost:8081/orders/compensate", null, String.class);
    }

    @Test
    public void testCreateOrderSaga_OrderFailure() {
        when(restTemplate.postForObject("http://localhost:8081/orders/create", null, String.class)).thenThrow(new RuntimeException("Order creation failed"));

        sagaService.createOrderSaga();

        verify(restTemplate, times(1)).postForObject("http://localhost:8081/orders/create", null, String.class);

        verify(restTemplate, never()).postForObject("http://localhost:8082/payments/process", null, String.class);
        verify(restTemplate, never()).postForObject("http://localhost:8083/inventory/reserve", null, String.class);
    }
}