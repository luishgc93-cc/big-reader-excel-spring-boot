package com.loader.loader;

import com.loader.loader.entity.Customer;
import com.loader.loader.repository.CustomerRepository;
import com.monitorjbl.xlsx.StreamingReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class LoaderApplication implements CommandLineRunner {

	@Autowired
	private final CustomerRepository customerRepository;

	public static void main(String[] args) {
		SpringApplication.run(LoaderApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		long startTimeRead = System.currentTimeMillis();

		log.info("Reading excel file : ");

        InputStream is = new FileInputStream("C:\\Users\\usuario\\Documents\\JAVA\\loader\\src\\main\\resources\\dataCustomers.xlsx");
		Workbook workbook = StreamingReader.builder()
				.rowCacheSize(50000)
				.bufferSize(131072)
				.open(is);

		List<Customer> customers = this.streamData(workbook);

		long endTimeRead = System.currentTimeMillis();
		log.info("Reading finished, time " + (endTimeRead - startTimeRead) + " ms");

		log.info("Inserting");
		long startTimeWrite = System.currentTimeMillis();

		customerRepository.saveAll(customers);

		long endTimeWrite = System.currentTimeMillis();
		log.info("Write finished, time " + (endTimeWrite - startTimeWrite) + " ms");
	}

	private List<Customer> streamData(Workbook workbook) {
		return StreamSupport.stream(workbook.spliterator(), false)
				.flatMap(sheet -> StreamSupport.stream(sheet.spliterator(), false)
						.skip(1)
						.map(con -> {
							Customer customer = new Customer();
							customer.setName(con.getCell(1).getStringCellValue());
							customer.setLastName(con.getCell(2).getStringCellValue());
							customer.setAddress(con.getCell(3).getStringCellValue());
							customer.setEmail(con.getCell(4).getStringCellValue());
							return customer;
						}))
				.collect(Collectors.toList());
	}
}
