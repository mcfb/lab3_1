package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.hamcrest.Matchers;
import org.junit.Test;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BookKeeperTest {

    @Test
    public void requestForInvoiceWithOnePositionShouldReturnInvoiceWithExactlyOnePosition() {

        BookKeeper bookKeeper = new BookKeeper(new InvoiceFactory());
        ClientData client = new ClientData(new Id("1"), "Maciej");



        TaxPolicy taxPolicy = mock(TaxPolicy.class);
        when(taxPolicy.calculateTax(ProductType.FOOD, new Money(2) ))
                .thenReturn(new Tax(new Money(.46), "23%" ));

        ProductData productData = mock(ProductData.class);
        when(productData.getType()).thenReturn(ProductType.FOOD);

        RequestItem requestItem = new RequestItem(productData, 1, new Money(2));
        InvoiceRequest invoiceRequest = new InvoiceRequest(client);
        invoiceRequest.add(requestItem);
        Invoice resultInvoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(resultInvoice.getItems().size(), Matchers.equalTo(1));



    }
}