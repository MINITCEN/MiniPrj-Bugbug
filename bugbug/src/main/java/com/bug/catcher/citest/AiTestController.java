import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.team.project.repository.ProductRepository;
import com.team.project.entity.Product;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class AiTestController {

    private final ProductRepository productRepository;

    // 1. @Transactional이 없어서 영속성 컨텍스트가 정상 작동 안 함
    // 2. 반복문 안에서 연관된 엔티티를 조회하여 MySQL에 N+1 쿼리 폭탄을 날림
    @GetMapping("/test/jpa")
    public String jpaTest() {
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            // FetchType.LAZY 상태인 연관 엔티티(예: Category)를 반복문 안에서 호출
            System.out.println(product.getCategory().getName());
        }

        return "JPA 폭탄 테스트 완료";
    }
}