package acme.features.inventor.quantity;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import acme.entities.items.Item;
import acme.entities.items.ItemType;
import acme.entities.quantities.Quantity;
import acme.entities.toolkits.Toolkit;
import acme.framework.components.models.Model;
import acme.framework.controllers.Errors;
import acme.framework.controllers.Request;
import acme.framework.services.AbstractUpdateService;
import acme.roles.Inventor;

@Service
public class InventorQuantityUpdateService implements AbstractUpdateService<Inventor, Quantity> {

	// Internal state ---------------------------------------------------------

	@Autowired
	protected InventorQuantityRepository repository;

	@Override
	public boolean authorise(final Request<Quantity> request) {
		assert request != null;

		boolean result;
		int toolkitId;
		Toolkit toolkit;

		toolkitId = request.getModel().getInteger("toolkitId");
		toolkit = this.repository.findOneToolkitById(toolkitId);
		result = (toolkit != null && toolkit.isDraftMode() && request.isPrincipal(toolkit.getInventor()));

		return result;
	}

	@Override
	public Quantity findOne(final Request<Quantity> request) {
		Quantity result;
		int id;

		id = request.getModel().getInteger("id");
		result = this.repository.findOneQuantityById(id);

		return result;
	}

	@Override
	public void bind(final Request<Quantity> request, final Quantity entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		request.bind(entity, errors, "toolkit.title", "number", "items", "toolkitId");
	}

	@Override
	public void validate(final Request<Quantity> request, final Quantity entity, final Errors errors) {
		assert request != null;
		assert entity != null;
		assert errors != null;

		Model model;
		Integer quantity;
		Item selectedItem;

		model = request.getModel();
		quantity = model.getInteger("number");
		selectedItem = this.repository.findOneItemByCode(model.getString("items"));

		if (!errors.hasErrors("number")) {
			errors.state(request, quantity != null && quantity >= 1, "number",
					"inventor.quantity.form.error.no-quantity");
			errors.state(request, selectedItem.getType().equals(ItemType.COMPONENT) || quantity == 1, "number",
					"inventor.quantity.form.error.repeated-tool");
		}

		if (!errors.hasErrors("items")) {
			int toolkitId;
			Collection<Item> itemsInToolkit;
			String newItemCurrency;

			newItemCurrency = selectedItem.getRetailPrice().getCurrency();

			// Take the currency of an existing item in the toolkit. Can be null if the
			// toolkit is empty
			toolkitId = request.getModel().getInteger("toolkitId");
			itemsInToolkit = this.repository.findItemsByToolkitId(toolkitId);

			errors.state(request,
					itemsInToolkit.isEmpty()
							|| itemsInToolkit.iterator().next().getRetailPrice().getCurrency().equals(newItemCurrency),
					"retailPrice", "inventor.quantity.form.error.wrong-currency");
		}
	}

	@Override
	public void unbind(final Request<Quantity> request, final Quantity entity, final Model model) {
		assert request != null;
		assert entity != null;
		assert model != null;

		request.unbind(entity, model, "toolkit.title", "number");

		final Collection<Item> items = this.repository.findAllItems();
		model.setAttribute("items", items);

		model.setAttribute("toolkitId", entity.getToolkit().getId());

	}

	@Override
	public void update(final Request<Quantity> request, final Quantity entity) {
		assert request != null;
		assert entity != null;

		this.repository.save(entity);
	}

}