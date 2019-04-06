package pl.com.bottega.ecommerce.sales.application.api.handler;

import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.util.reflection.Whitebox;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.ClientData;
import pl.com.bottega.ecommerce.canonicalmodel.publishedlanguage.Id;
import pl.com.bottega.ecommerce.sales.application.api.command.AddProductCommand;
import pl.com.bottega.ecommerce.sales.domain.client.Client;
import pl.com.bottega.ecommerce.sales.domain.client.ClientRepository;
import pl.com.bottega.ecommerce.sales.domain.equivalent.SuggestionService;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.Product;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductRepository;
import pl.com.bottega.ecommerce.sales.domain.productscatalog.ProductType;
import pl.com.bottega.ecommerce.sales.domain.reservation.Reservation;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservationRepository;
import pl.com.bottega.ecommerce.sales.domain.reservation.ReservedProduct;
import pl.com.bottega.ecommerce.sharedkernel.Money;
import pl.com.bottega.ecommerce.system.application.SystemContext;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AddProductCommandHandlerTest {


    @Test
    public void shouldLoadThenAddProductAndSaveReservationOnce() {

        AddProductCommandHandler handler = new AddProductCommandHandler();
        Product product = new Product(new Id("1"), new Money(10), "myProduct", ProductType.STANDARD);
        Reservation reservation = mock(Reservation.class);
        when(reservation.getStatus()).thenReturn(Reservation.ReservationStatus.OPENED);
        when(reservation.isClosed()).thenReturn(Boolean.FALSE);
        when(reservation.getClientData()).thenReturn(new ClientData(Id.generate(), "Client2"));
        when(reservation.getCreateDate()).thenReturn(new Date());

        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        Whitebox.setInternalState(handler, "reservationRepository", reservationRepository);
        when(reservationRepository.load(any(Id.class))).thenReturn(reservation);

        ProductRepository productRepository = mock(ProductRepository.class);
        Whitebox.setInternalState(handler, "productRepository", productRepository);
        when(productRepository.load(any(Id.class))).thenReturn(product);

        AddProductCommand command = new AddProductCommand(new Id("1"), new Id("1"), 1);
        handler.handle(command);
        verify(reservationRepository, times(1)).load(any(Id.class));
        verify(reservation, times(1)).add(product, 1);
        verify(reservationRepository, times(1)).save(any(Reservation.class));

    }
    

    @Test
    public void shouldSuggestDifferentProductIfOneIsNotAvailable() {

        AddProductCommandHandler handler = new AddProductCommandHandler();
        Product productSuggested = new Product(new Id("1"), new Money(10), "myProduct", ProductType.STANDARD);
        Product productWanted = spy(new Product(new Id("2"), new Money(12), "betterProduct", ProductType.STANDARD));
        when(productWanted.isAvailable()).thenReturn(false);
        Client client = mock(Client.class);
        Reservation reservation = mock(Reservation.class);

        ClientRepository clientRepository = mock(ClientRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        ProductRepository productRepository = mock(ProductRepository.class);
        SuggestionService suggestionService = mock(SuggestionService.class);
        SystemContext systemContext = new SystemContext();
        Whitebox.setInternalState(handler, "clientRepository", clientRepository);
        Whitebox.setInternalState(handler, "reservationRepository", reservationRepository);
        Whitebox.setInternalState(handler, "productRepository", productRepository);
        Whitebox.setInternalState(handler, "suggestionService", suggestionService);
        Whitebox.setInternalState(handler, "systemContext", systemContext);

        AddProductCommand command = new AddProductCommand(new Id("1"), new Id("2"), 1);
        when(clientRepository.load(any(Id.class))).thenReturn(client);
        when(reservationRepository.load(command.getOrderId())).thenReturn(reservation);
        when(productRepository.load(command.getProductId())).thenReturn(productWanted);
        when(suggestionService.suggestEquivalent(productWanted, client)).thenReturn(productSuggested);
        when(productWanted.isAvailable()).thenReturn(false);

        handler.handle(command);
        verify(suggestionService, times(1)).suggestEquivalent(productWanted, client);
    }

}