import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.util.List;

public class TestMethod {

    @Test
    public void testJdbc() {

        ApplicationContext context = new ClassPathXmlApplicationContext("classpath:spring/applicationContext-dao.xml");
        TbBrandMapper mapper = context.getBean(TbBrandMapper.class);
        List<TbBrand> list=mapper.selectByExample(null);
        System.out.println(list.size());

    }
}
