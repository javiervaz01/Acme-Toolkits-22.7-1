package acme.features.inventor.item;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.components.ExchangeService;
import acme.entities.items.Item;
import acme.entities.quantities.Quantity;
import acme.entities.toolkits.Toolkit;
import acme.framework.components.models.Model;
import acme.framework.controllers.Request;
import acme.framework.datatypes.Money;
import acme.framework.helpers.CollectionHelper;
import acme.framework.services.AbstractListService;
import acme.roles.Inventor;

@Service
public class InventorItemListByToolkitService implements AbstractListService<Inventor, Item> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected InventorItemRepository repository;

	@Autowired
	protected ExchangeService exchangeService;

	@Override
	public boolean authorise(final Request<Item> request) {
		assert request != null;

		int id;

		id = request.getModel().getInteger("masterId");
		final Toolkit requested = this.repository.findOneToolkitById(id);

		return !requested.isDraftMode() || request.isPrincipal(requested.getInventor());
	}

	@Override
	public Collection<Item> findMany(final Request<Item> request) {
		assert request != null;

		Collection<Item> result;
		final int id;

		id = request.getModel().getInteger("masterId");
		result = this.repository.findItemsByToolkitId(id);

		return result;
	}

	@Override
	public void unbind(final Request<Item> request, final Collection<Item> entities, final Model model) {
		assert request != null;
		assert !CollectionHelper.someNull(entities);
		assert model != null;

		int toolkitId;
		Toolkit toolkit;
		final boolean showAddItem;

		toolkitId = request.getModel().getInteger("masterId");
		toolkit = this.repository.findOneToolkitById(toolkitId);
		showAddItem = (toolkit.isDraftMode() && request.isPrincipal(toolkit.getInventor()));

		model.setAttribute("toolkitId", toolkitId);
		model.setAttribute("showAddItem", showAddItem);
	}

	@Override
	public void unbind(final Request<Item> request, final Item entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "name", "code", "technology", "description", "retailPrice", "info", "type");

		final Integer id = entity.getId();
		final Quantity quantity = this.repository.findQuantityByItemId(id);
		model.setAttribute("quantity", quantity.getNumber());

		final Money exchange = this.exchangeService.getExchange(entity.getRetailPrice());
		model.setAttribute("exchange", exchange);
	}
}
