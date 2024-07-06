package saga.orchestrator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saga")
public class SagaController {

    @Autowired
    private SagaService sagaService;

    @GetMapping("/createOrder")
    public String createOrderSaga() {
        sagaService.createOrderSaga();
        return "Saga execution completed";
    }
}