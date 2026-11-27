package be.shwan.sideproject.support;

import be.shwan.sideproject.TestcontainersConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
public abstract class RepositoryTestSupport {
  @Autowired protected TestEntityManager em;
}
