package pl.com.bottega.ecommerce.sales.domain.invoicing;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.mockito.AdditionalMatchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductData;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sharedkernel.Money;

import java.io.ObjectInputStream;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BookKeeperTest {

    @Test
    public void requestForInvoiceWithOnePositionShouldReturnInvoiceWithExactlyOnePosition() {

        BookKeeper bookKeeper = new BookKeeper(new InvoiceFactory());
        ClientData client = new ClientData(new Id("1"), "Maciej");


        TaxPolicy taxPolicy = mock(TaxPolicy.class);
        when(taxPolicy.calculateTax(ProductType.FOOD, new Money(2)))
                .thenReturn(new Tax(new Money(.46), "23%"));

        ProductData productData = mock(ProductData.class);
        when(productData.getType()).thenReturn(ProductType.FOOD);

        RequestItem requestItem = new RequestItem(productData, 1, new Money(2));
        InvoiceRequest invoiceRequest = new InvoiceRequest(client);
        invoiceRequest.add(requestItem);
        Invoice resultInvoice = bookKeeper.issuance(invoiceRequest, taxPolicy);

        assertThat(resultInvoice.getItems().size(), Matchers.equalTo(1));


    }


    @Test
    public void requestForInvoiceWithTwoPositionShouldCallCalculateTaxMethodTwice() {
        BookKeeper bookKeeper = new BookKeeper(new InvoiceFactory());
        ClientData client = new ClientData(new Id("1"), "Maciej");

        TaxPolicy taxPolicy = mock(TaxPolicy.class);
        when(taxPolicy.calculateTax(ProductType.FOOD, new Money(2)))
                .thenReturn(new Tax(new Money(.46), "23%"));
        when(taxPolicy.calculateTax(ProductType.DRUG, new Money(100)))
                .thenReturn(new Tax(new Money(8), "8%"));

        ProductData productDataFood = mock(ProductData.class);
        when(productDataFood.getType()).thenReturn(ProductType.FOOD);
        ProductData productDataDrug = mock(ProductData.class);
        when(productDataDrug.getType()).thenReturn(ProductType.DRUG);

        InvoiceRequest invoiceRequest = new InvoiceRequest(client);
        RequestItem requestItemFood = new RequestItem(productDataFood, 1, new Money(2));
        RequestItem requestItemDrug = new RequestItem(productDataDrug, 1, new Money(100));
        invoiceRequest.add(requestItemFood);
        invoiceRequest.add(requestItemDrug);
        bookKeeper.issuance(invoiceRequest, taxPolicy);


        ArgumentCaptor<ProductType> arg1 = ArgumentCaptor.forClass(ProductType.class);
        ArgumentCaptor<Money> arg2 = ArgumentCaptor.forClass(Money.class);

        verify(taxPolicy, times(2)).calculateTax(arg1.capture(), arg2.capture());
        assertThat(arg1.getAllValues().get(0), Matchers.equalTo(ProductType.FOOD));
        assertThat(arg2.getAllValues().get(0), Matchers.equalTo(new Money(2)));
        assertThat(arg1.getAllValues().get(1), Matchers.equalTo(ProductType.DRUG));
        assertThat(arg2.getAllValues().get(1), Matchers.equalTo(new Money(100)));

    }


}