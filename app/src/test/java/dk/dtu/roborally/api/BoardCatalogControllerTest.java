package dk.dtu.roborally.api;

import dk.dtu.roborally.api.controllers.BoardCatalogController;
import dk.dtu.roborally.loaders.BoardCatalog;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BoardCatalogController.class)
@Import(BoardCatalogControllerTest.Config.class)
class BoardCatalogControllerTest {

	@TestConfiguration
	static class Config {
		@Bean
		BoardCatalog boardCatalog() {
			return BoardCatalog.load();
		}
	}

	@Autowired
	private MockMvc mockMvc;

	@Test
	void listsBoardsWithIdAndNameOnly() throws Exception {
		mockMvc.perform(get("/api/boards")).andExpect(status().isOk())
				.andExpect(jsonPath("$[0].id").value("level_0"))
				.andExpect(jsonPath("$[0].name").value("Conveyor cyclus"))
				.andExpect(jsonPath("$[0].path").doesNotExist())
				.andExpect(jsonPath("$[0].json").doesNotExist());
	}
}
