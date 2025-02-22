package com.petrolpark.destroy.chemistry.legacy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.petrolpark.destroy.Destroy;
import com.petrolpark.destroy.chemistry.api.error.ChemistryException;
import com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction;
import com.petrolpark.destroy.chemistry.legacy.index.DestroyMolecules;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

/**
 * A Reaction takes place between specific {@link LegacySpecies Molecules}, and produces specific Molecules.
 * This is in contrast with {@link com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction Generic
 * Reactions}, which essentially function as Reaction generators, and will generate Reactions based on the
 * available Molecules in a {@link LegacyMixture}.
 */
public class LegacyReaction {

    public static final float GAS_CONSTANT = 8.3145f;

    /**
     * The set of all Reactions known to Destroy, indexed by their {@link id IDs}.
     */
    public static final Map<String, LegacyReaction> REACTIONS = new HashMap<>();

    public static ReactionBuilder generatedReactionBuilder() {
        return new ReactionBuilder(new LegacyReaction("novel"), true, false);
    };

    private Map<LegacySpecies, Integer> reactants, products, orders;

    /**
     * All {@link IItemReactant Item Reactants} (and catalysts) this Reaction.
     */
    private List<IItemReactant> itemReactants;
    /**
     * The number of moles of Reaction which will occur if all {@link LegacyReaction#itemReactants Item requirements} are met.
     */
    private float molesPerItem;

    /**
     * Whether this Reaction needs UV light to proceed.
     */
    private boolean isCatalysedByUV;

    /**
     * The {@link ReactionResult Reaction Result} of this Reaction, if there is one.
     */
    private ReactionResult result;

    // THERMODYNAMICS

    /**
     * {@code A} in {@code k = Aexp(-E/RT)}.
     */
    private float preexponentialFactor;
    /**
     * {@code E} in {@code k = Aexp(-E/RT)}, in kJ/mol/s.
     */
    private float activationEnergy;
    /**
     * The change in enthalpy (in kJ/mol) for this Reaction.
     */
    private float enthalpyChange;
    /**
     * The half-cell potential of this Reaction under standard conditions, if applicable, relative to the standard hydrogen electrode.
     */
    private float standardHalfCellPotential;
    /**
     * If this is a half-cell reduction, this is how many electrons are on the left hand side of the reaction.
     */
    private int electrons;

    /**
     * The namespace of the mod by which this Reaction was declared, or {@code novel} if this was generated
     * by a {@link com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction Reaction generator}.
     */
    private String nameSpace;
    /**
     * The ID of this reaction, not including its {@link LegacyReaction#nameSpace name space}, and {@code null} if this
     * Reaction was generated by a {@link com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction Reaction
     * generator}.
     * @see LegacyReaction#getId The getter for this field
     */
    private String id;

    // JEI DISPLAY INFORMATION

    /**
     * Whether this Reaction should be shown in JEI. Examples of Reactions which shouldn't be shown are {@link
     * com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction generated Reactions}, and reverse Reactions
     * whose corresponding Reactions are already shown in JEI.
     */
    private Supplier<Boolean> includeInJei;
    /**
     * Whether this Reaction should use an equilibrium arrow when displayed in JEI, rather than the normal one. This
     * is just for display, and has no effect on the behaviour of the Reaction in a {@link LegacyMixture}.
     */
    private boolean displayAsReversible;
    /**
     * If this is the 'forward' half of a reversible Reaction, this points to the reverse Reaction. This is so JEI
     * knows the products of the forward Reaction are the reactants of the reverse, and vice versa.
     */
    private LegacyReaction reverseReaction;

    /**
     * Get the Reaction with the given {@link LegacyReaction#getFullId ID}.
     * @param reactionId In the format {@code <namespace>:<id>}
     * @return {@code null} if no Reaction exists with that ID
     */
    public static LegacyReaction get(String reactionId) {
        return REACTIONS.get(reactionId);
    };

    protected LegacyReaction(String nameSpace) {
        this.nameSpace = nameSpace;
    };

    /**
     * Whether this Molecule gets consumed in this Reaction (does not include catalysts).
     */
    public Boolean containsReactant(LegacySpecies molecule) {
        return this.reactants.keySet().contains(molecule);
    };

    /**
     * Whether this Molecule is created in this Reaction.
     */
    public Boolean containsProduct(LegacySpecies molecule) {
        return this.products.keySet().contains(molecule);
    };

    /**
     * All Molecules which are consumed in this Reaction (but not their molar ratios).
     */
    public Set<LegacySpecies> getReactants() {
        return this.reactants.keySet();
    };

    /**
     * Whether this Reaction needs any Item Stack as a {@link IItemReactant reactant}. Even if this is
     * {@code true}, the Reaction may still have {@link IItemReactant#isCatalyst Item Stack catalysts}.
     */
    public boolean consumesItem() {
        for (IItemReactant itemReactant : itemReactants) {
            if (!itemReactant.isCatalyst()) return true;
        };
        return false;
    };

    /**
     * Get the {@link IItemReactant required Items} for this Reaction.
     */
    public List<IItemReactant> getItemReactants() {
        return itemReactants;
    };

    /**
     * Get the moles of this Reaction that will occur once all {@link LegacyReaction#itemReactants Item requirements} are fulfilled.
     */
    public float getMolesPerItem() {
        return molesPerItem;
    };

    /**
     * Whether this Reaction needs UV light to proceed.
     */
    public boolean needsUV() {
        return isCatalysedByUV;
    };

    /**
     * All Molecules which are created in this Reaction (but not their molar ratios).
     */
    public Set<LegacySpecies> getProducts() {
        return this.products.keySet();
    };

    /**
     * Get the {@link LegacyReaction#activationEnergy activation energy} for this Reaction, in kJ.
     * @see LegacyReaction#getRateConstant Arrhenius equation
     */
    public float getActivationEnergy() {
        return activationEnergy;
    };

    /**
     * Get the {@link LegacyReaction#preexponentialFactor preexponential} for this Reaction, in mol/B/s.
     * @see LegacyReaction#getRateConstant Arrhenius equation
     */
    public float getPreexponentialFactor() {
        return preexponentialFactor;
    };

    /**
     * The rate constant of this Reaction at the given temperature.
     * @param temperature (in kelvins).
     */
    public float getRateConstant(float temperature) {
        return preexponentialFactor * (float)Math.exp(-((activationEnergy * 1000) / (GAS_CONSTANT * temperature)));
    };

    /**
     * The {@link LegacyReaction#enthalpyChange enthalpy change} for this Reaction, in kJ/mol.
     */
    public float getEnthalpyChange() {
        return enthalpyChange;
    };

    /**
     * Whether this Reaction has a {@link ReactionResult Result}. 
     */
    public boolean hasResult() {
        return result != null;
    };

    /**
     * The {@link ReactionResult Result} of this Reaction, which occurs once a set
     * number of moles of Reaction have occured.
     * @return {@code null} if this Reaction has no result.
     */
    public ReactionResult getResult() {
        return result;
    };

    /**
     * The unique identifier for this Reaction (not including its namespace), which
     * also acts as its translation key. {@code <namespace>.reaction.<id>} should hold
     * the name of this Reaction, and {@code <namespace>.reaction.<id>.description}
     * should hold the description of this Reaction.
     * @see LegacyReaction#getFullId Get the full ID
     */
    public String getId() {
        return id;
    };

    /**
     * Get the fully unique ID for this Reaction, in the format {@code <namespace>:
     * <id>}, for example {@code destroy:chloroform_fluorination}.
     */
    public String getFullId() {
        return nameSpace + ":" + id;
    };

    /**
     * Whether this Reaction should be displayed in the list of Reactions in JEI.
     */
    public boolean includeInJei() {
        return includeInJei.get();
    };

    /**
     * Whether this Reaction should be displayed in JEI with an equilibrium arrow rather than a normal one.
     */
    public boolean displayAsReversible() {
        return displayAsReversible;
    };

    /**
     * If this is the 'forward' half of a reversible Reaction, this contains the reverse Reaction. This is so JEI
     * knows the products of the forward Reaction are the reactants of the reverse, and vice versa. If this is not
     * part of a reversible Reaction, this is empty. This is just for display; if a Reaction has a reverse and is needed
     * for logic (e.g. Reacting in a Mixture) it should not be accessed in this way.
     */
    public Optional<LegacyReaction> getReverseReactionForDisplay() {
        return Optional.ofNullable(reverseReaction);
    };

    /**
     * Return the Reaction which has an indexed Reaction Recipe that is displayed in JEI.
     * Usually, that will be this Reaction. Sometimes it will be the reverse, and sometimes it will be {@code null}.
     * @return The Reaction which appears in JEI, or {@code null}
     */
    public LegacyReaction getReactionDisplayedInJEI() {
        if (includeInJei.get()) return this;
        return getReverseReactionForDisplay().map(reaction -> reaction.includeInJei.get() ? reaction : null).orElse(null);
    };

    /**
     * The name space of the mod by which this Reaction was defined.
     * @return {@code "novel"} if this was generated automatically by a {@link com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction Reaction generator}.
     */
    public String getNameSpace() {
        return nameSpace;
    };

    /**
     * Get the stoichometric ratio of this {@link LegacySpecies reactant} or catalyst in this Reaction.
     * @param reactant
     * @return {@code 0} if this Molecule is not a reactant
     */
    public Integer getReactantMolarRatio(LegacySpecies reactant) {
        if (!reactants.keySet().contains(reactant)) {
            return 0;
        } else {
            return reactants.get(reactant);
        }
    };

    /**
     * Get the stoichometric ratio of this {@link LegacySpecies product} in this Reaction.
     * @param product
     * @return {@code 0} if this Molecule is not a product
     */
    public Integer getProductMolarRatio(LegacySpecies product) {
        if (!products.keySet().contains(product)) {
            return 0;
        } else {
            return products.get(product);
        }
    };

    /**
     * Get every {@link LegacySpecies reactant} and catalyst in this Reaction, mapped to their
     * orders in the rate equation.
     */
    public Map<LegacySpecies, Integer> getOrders() {
        return this.orders;
    };

    /**
     * Get the {@link LegacyReaction#standardHalfCellPotential standard half cell potential} of this reduction half-Reaction.
     * @return {@code 0f} if this is not a reduction half-Reaction
     */
    public float getStandardHalfCellPotential() {
        return standardHalfCellPotential;
    };

    /**
     * Get the {@link LegacyReaction#electrons transferred} in this reduction half-Reaction.
     * @return 0 if this is not a reduction half-Reaction
     */
    public int getElectronsTransferred() {
        return electrons;
    };

    /**
     * Whether this Reaction is a reduction half-Reaction.
     */
    public boolean isHalfReaction() {
        return electrons != 0;
    };

    /**
     * A class for constructing {@link LegacyReaction Reactions}.
     * <ul>
     * <li>If this is for a Reaction with named {@link LegacySpecies Molecules}, {@link ReactionBuilder#ReactionBuilder(String) instantiate with a name space}.</li>
     * <li>If this is for a {@link com.petrolpark.destroy.chemistry.legacy.genericreaction.GenericReaction generated} Reaction, get a builder {@link LegacyReaction#generatedReactionBuilder here}.</li>
     * </ul> 
     * A new builder must be used for each Reaction.
     */
    public static class ReactionBuilder {

        private String namespace;

        /**
         * Whether this Reaction is being generated by a {@link GenericReaction Generic Reaction generator}.
         */
        private final boolean generated;
        private final LegacyReaction reaction;

        private final boolean declaredAsReverse;

        private boolean hasForcedPreExponentialFactor;
        private boolean hasForcedActivationEnergy;
        private boolean hasForcedEnthalpyChange;
        private boolean hasForcedHalfCellPotential;

        private ReactionBuilder(LegacyReaction reaction, boolean generated, boolean declaredAsReverse) {
            this.generated = generated;
            this.reaction = reaction;
            this.declaredAsReverse = declaredAsReverse;

            reaction.reactants = new HashMap<>();
            reaction.products = new HashMap<>();
            reaction.orders = new HashMap<>();

            reaction.itemReactants = new ArrayList<>();
            reaction.molesPerItem = 0f;

            reaction.includeInJei = () -> !generated && !declaredAsReverse;
            reaction.displayAsReversible = false;

            hasForcedPreExponentialFactor = false;
            hasForcedActivationEnergy = false;
            hasForcedEnthalpyChange = false;
            hasForcedHalfCellPotential = false;
        };

        public ReactionBuilder(String namespace) {
            this(new LegacyReaction(namespace), false, false);
            this.namespace = namespace;
        };

        private void checkNull(LegacySpecies molecule) {
            if (molecule == null) throw e("Molecules cannot be null");
        };

        /**
         * Add a {@link LegacySpecies} of which one mole will be consumed per mole of Reaction.
         * By default, the order of rate of reaction with respect to this Molecule will be one.
         * @param molecule
         * @return This Reaction Builder
         * @see ReactionBuilder#addReactant(LegacySpecies, int) Adding a different stoichometric ratio
         * @see ReactionBuilder#addReactant(LegacySpecies, int, int) Adding a Molecule with a different order
         */
        public ReactionBuilder addReactant(LegacySpecies molecule) {
            return addReactant(molecule, 1);
        };

        /**
         * Add a {@link LegacySpecies} of which {@code ratio} moles will be consumed per mole of Reaction.
         * By default, the order of rate of reaction with respect to this Molecule will be one.
         * @param molecule
         * @param ratio The stoichometric ratio of this reactant in this Reaction
         * @return This Reaction Builder
         * @see ReactionBuilder#addReactant(LegacySpecies, int, int) Adding a Molecule with a different order
         */
        public ReactionBuilder addReactant(LegacySpecies molecule, int ratio) {
            return addReactant(molecule, ratio, ratio);
        };

        /**
         * Add a {@link LegacySpecies} which will be consumed in this Reaction.
         * @param molecule
         * @param ratio The stoichometric ratio of this reactant in this Reaction
         * @param order The order of the rate of the Reaction with respect to this Molecule
         * @return This Reaction Builder
         */
        public ReactionBuilder addReactant(LegacySpecies molecule, int ratio, int order) {
            checkNull(molecule);
            reaction.reactants.put(molecule, ratio);
            reaction.orders.put(molecule, order);
            return this;
        };

        /**
         * Sets the order of rate of Reaction of the given {@link LegacySpecies}.
         * @param molecule If this is not a reactant of this Reaction, an error will be thrown
         * @param order
         * @return This Reaction Builder
         * @see ReactionBuilder#addCatalyst(LegacySpecies, int) Adding order with respect to a Molecule that is not a reactant (i.e. a catalyst)
         */
        public ReactionBuilder setOrder(LegacySpecies molecule, int order) {
            if (!reaction.reactants.keySet().contains(molecule)) throw e("Cannot modify order of a Molecule ("+ molecule.getFullID() +") that is not a reactant.");
            addCatalyst(molecule, order);
            return this;
        };

        /**
         * Adds an {@link IItemReactant Item Reactant} (or catalyst) to this {@link LegacyReaction}.
         * @param itemReactant The Item Reactant
         * @param moles The {@link LegacyReaction#getMolesPerItem moles of Reaction} which will occur if all necessary Item Reactants are present. If this
         * Reaction has multiple Item Reactants, this must be the same each time.
         * @return This Reaction Builder
         * @see ReactionBuilder#addSimpleItemReactant Adding a single Item as a Reactant
         * @see ReactionBuilder#addSimpleItemTagReactant Adding an Item Tag as a Reactant
         */
        public ReactionBuilder addItemReactant(IItemReactant itemReactant, float moles) {
            if (reaction.molesPerItem != 0f && reaction.molesPerItem != moles) throw e("The number of moles of Reaction which occur when all Item Requirements are met is constant for a Reaction, not individual per Item Reactant. The same number must be supplied each time an Item Reactant is added.");
            reaction.molesPerItem = moles;
            reaction.itemReactants.add(itemReactant);
            return this;
        };

        /**
         * Adds an Item as a {@link IItemReactant reactant} for this {@link LegacyReaction}. An Item Stack of size {@code 1},
         * with this Item will be consumed in the Reaction.
         * @param item
         * @param moles The {@link LegacyReaction#getMolesPerItem moles of Reaction} which will occur if all necessary Item Reactants are present. If this
         * Reaction has multiple Item Reactants, this must be the same each time.
         * @return This Reaction Builder
         */
        public ReactionBuilder addSimpleItemReactant(Supplier<Item> item, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemReactant(item), moles);
        };

        /**
         * Adds an Item Tag as a {@link IItemReactant reactant} for this {@link LegacyReaction}. An Item Stack of size {@code 1},
         * containing an Item with this Tag will be consumed in the Reaction.
         * @param tag
         * @param moles The {@link LegacyReaction#getMolesPerItem moles of Reaction} which will occur if all necessary Item Reactants are present. If this
         * Reaction has multiple Item Reactants, this must be the same each time.
         * @return This Reaction Builder
         */
        public ReactionBuilder addSimpleItemTagReactant(TagKey<Item> tag, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemTagReactant(tag), moles);
        };

        /**
         * Adds an Item as a {@link IItemReactant catalyst} for this {@link LegacyReaction}. An Item Stack containing the Item
         * must be present for the Reaction to occur.
         * @param item
         * @param moles The {@link LegacyReaction#getMolesPerItem moles of Reaction} which will occur if all necessary Item Reactants are present. If this
         * Reaction has multiple Item Reactants, this must be the same each time.
         * @return This Reaction Builder
         */
        public ReactionBuilder addSimpleItemCatalyst(Supplier<Item> item, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemCatalyst(item), moles);
        };

        /**
         * Adds an Item Ta as a {@link IItemReactant catalyst} for this {@link LegacyReaction}. An Item Stack containing Items with the tag
         * must be present for the Reaction to occur.
         * @param tag
         * @param moles The {@link LegacyReaction#getMolesPerItem moles of Reaction} which will occur if all necessary Item Reactants are present. If this
         * Reaction has multiple Item Reactants, this must be the same each time.
         * @return This Reaction Builder
         */
        public ReactionBuilder addSimpleItemTagCatalyst(TagKey<Item> tag, float moles) {
            return addItemReactant(new IItemReactant.SimpleItemTagCatalyst(tag), moles);
        };

        /**
         * Set this Reaction as requiring ultraviolet light, from the sun or a Blacklight.
         * If {@code true}, the rate of this Reaction will be multiplied by {@code 0} to {@code 1} depending on the amount of incident UV.
         * @return This Reaction Builder
         */
        public ReactionBuilder requireUV() {
            reaction.isCatalysedByUV = true;
            return this;
        };

        /**
         * Add a {@link LegacySpecies} of which one mole will be produced per mole of Reaction.
         * @param molecule
         * @return This Reaction Builder
         */
        public ReactionBuilder addProduct(LegacySpecies molecule) {
            return addProduct(molecule, 1);
        };

        /**
         * Add a {@link LegacySpecies} of which {@code ratio} moles will be produced per mole of Reaction.
         * @param molecule
         * @param ratio The stoichometric ratio of this product in this Reaction
         * @return This Reaction Builder
         * @see ReactionBuilder#addProduct(LegacySpecies, int) Adding a different stoichometric ratio
         */
        public ReactionBuilder addProduct(LegacySpecies molecule, int ratio) {
            checkNull(molecule);
            reaction.products.put(molecule, ratio);
            return this;
        };

        /**
         * Add a {@link LegacySpecies} which does not get consumed in this Reaction, but which affects the rate.
         * @param molecule
         * @param order If this is is 0, the rate will not be affected but the Molecule will need to be present for the Reaction to proceed
         * @return This Reaction Builder
         */
        public ReactionBuilder addCatalyst(LegacySpecies molecule, int order) {
            checkNull(molecule);
            reaction.orders.put(molecule, order);
            return this;
        };

        /**
         * Include this Reaction in JEI only if the condition is matched.
         * @return This Reaction Builder
         */
        public ReactionBuilder includeInJeiIf(Supplier<Boolean> condition) {
            reaction.includeInJei = condition;
            return this;
        };

        /**
         * Don't include this Reaction in the list of Reactions shown in JEI.
         * @return This Reaction Builder
         */
        public ReactionBuilder dontIncludeInJei() {
            reaction.includeInJei = () -> false;
            return this;
        };

        /**
         * Show a double-headed arrow for this Reaction in JEI. To actually make this a reversible reaction, use {@link ReactionBuilder#reverseReaction this}.
         * @return This Reaction Builder
         */
        public ReactionBuilder displayAsReversible() {
            reaction.displayAsReversible = true;
            return this;
        };

        /**
         * Set the ID for the Reaction. The title and description of the Reaction will be looked for at {@code "<namespace>.reaction.<id>"}.
         * @param id A unique string
         * @return This Reaction Builder
         */
        public ReactionBuilder id(String id) {
            reaction.id = id;
            return this;
        };

        /**
         * Set the pre-exponential factor in the Arrhenius equation for this Reaction.
         * @param preexponentialFactor
         * @return This Reaction Builder
         */
        public ReactionBuilder preexponentialFactor(float preexponentialFactor) {
            reaction.preexponentialFactor = preexponentialFactor;
            hasForcedPreExponentialFactor = true;
            return this;
        };

        /**
         * Set the activation energy (in kJ) for this Reaction.
         * If no activation energy is given, defaults to 50kJ.
         * @param activationEnergy
         * @return This Reaction Builder
         */
        public ReactionBuilder activationEnergy(float activationEnergy) {
            reaction.activationEnergy = activationEnergy;
            hasForcedActivationEnergy = true;
            return this;
        };

        /**
         * Set the enthalpy change (in kJ/mol) for this Reaction.
         * If no enthalpy change is given, defaults to 0kJ.
         * @param enthalpyChange
         * @return This Reaction Builder
         */
        public ReactionBuilder enthalpyChange(float enthalpyChange) {
            reaction.enthalpyChange = enthalpyChange;
            hasForcedEnthalpyChange = true;
            return this;
        };

        /**
         * Set the standard half-cell potential, in V, relative to the standard hydrogen electrode.
         * @param standardHalfCellPotential
         * @return This Reaction Builder
         */
        public ReactionBuilder standardHalfCellPotential(float standardHalfCellPotential) {
            if (hasForcedHalfCellPotential) throw e("Cannot set half-cell potential more than once.");
            reaction.standardHalfCellPotential = standardHalfCellPotential;
            hasForcedHalfCellPotential = true;
            return this;
        };

        /**
         * Set the {@link ReactionResult Reaction Result} for this Reaction.
         * Use a {@link com.petrolpark.destroy.chemistry.legacy.reactionresult.CombinedReactionResult CombinedReactionResult} to set multiple
         * @return This Reaction Builder
         */
        public ReactionBuilder withResult(float moles, BiFunction<Float, LegacyReaction, ReactionResult> reactionresultFactory)  {
            if (reaction.result != null) throw e("Reaction already has a Reaction Result. Use a CombinedReactionResult to have multiple.");
            reaction.result = reactionresultFactory.apply(moles, reaction);
            return this;
        };

        /**
         * Registers an acid. This automatially registers four {@link LegacyReaction Reactions} (one for the association,
         * two for the dissociation (with both water and hydroxide)).
         * The pKa is assumed to be temperature-independent - if this is not wanted, manually register the two Reactions.
         * @param acid
         * @param conjugateBase This should have a charge one less than the acid and should ideally conserve Atoms
         * @param pKa
         * @return The dissociation Reaction
         */
        public LegacyReaction acid(LegacySpecies acid, LegacySpecies conjugateBase, float pKa) {

            if (conjugateBase.getCharge() + 1 != acid.getCharge()) throw e("Acids must not violate the conservation of charge.");

            // Dissociation with water
            LegacyReaction dissociationReaction = this
                .id(acid.getFullID().split(":")[1] + ".dissociation")
                .addReactant(acid)
                .addCatalyst(DestroyMolecules.WATER, 1)
                .addProduct(DestroyMolecules.PROTON)
                .addProduct(conjugateBase)
                .activationEnergy(GAS_CONSTANT * 0.298f) // Makes the pKa accurate at room temperature
                .preexponentialFactor(0.5f * (float)Math.pow(10, -pKa))
                .dontIncludeInJei()
                .build();
            
            // Neutralization with hydroxide (temporary fix while API gets rewritten)
            new ReactionBuilder(namespace)
                .id(acid.getFullID().split(":")[1] + ".neutralization")
                .addReactant(acid)
                .addReactant(DestroyMolecules.HYDROXIDE)
                .addProduct(conjugateBase)
                .addProduct(DestroyMolecules.WATER)
                .activationEnergy(GAS_CONSTANT * 0.298f) // Makes the pKa accurate at room temperature
                .preexponentialFactor(0.5f * (float)Math.pow(10, -pKa))
                .dontIncludeInJei()
                .build();

            // Association
            new ReactionBuilder(namespace)
                .id(acid.getFullID().split(":")[1] + ".association")
                .addReactant(conjugateBase)
                .addReactant(DestroyMolecules.PROTON)
                .addProduct(acid)
                .activationEnergy(GAS_CONSTANT * 0.298f)
                .preexponentialFactor(1f)
                .dontIncludeInJei()
                .build();

            return dissociationReaction;
        };

        /**
         * @see ReactionBuilder#reverseReaction(Consumer)
         */
        public ReactionBuilder reversible() {
            return reverseReaction(r -> {});
        };

        /**
         * Register a reverse Reaction for this Reaction.
         * <p>This reverse Reaction will have opposite {@link ReactionBuilder#addReactant reactants} and {@link ReactionBuilder#addProduct products},
         * but all the same {@link ReactionBuilder#addCatalyst catalysts}. It will {@link ReactionBuilder#dontIncludeInJei not be shown in JEI}, but
         * the original Reaction will include the reverse symbol.</p>
         * <p>The reverse Reaction does not automatically add Item Stack reactants, products or catalysts, or UV requirements.</p>
         * @param reverseReactionModifier A consumer which gets passed the Builder of the reverse Reaction once its reactants, products and catalysts
         * have been added, and the {@link ReactionBuilder#enthalpyChange enthalpy change} and {@link ReactionBuilder#activationEnergy activation energy}
         * have been set, if applicable. This allows you to add {@link ReactionBuilder#setOrder orders with respect to the new reactants}, and {@link
         * ReactionBuilder#withResult 
         * Reaction results}.
         * @return This Reaction Builder (not the reverse Reaction Builder)
         */
        public ReactionBuilder reverseReaction(Consumer<ReactionBuilder> reverseReactionModifier) {
            if (generated) throw e("Generated Reactions cannot be reversible. Add another Generic Reaction instead.");
            reaction.displayAsReversible = true;
            ReactionBuilder reverseBuilder = new ReactionBuilder(new LegacyReaction(namespace), false, true);
            for (Entry<LegacySpecies, Integer> reactant : reaction.reactants.entrySet()) {
                reverseBuilder.addProduct(reactant.getKey(), reactant.getValue());
            };
            for (Entry<LegacySpecies, Integer> product : reaction.products.entrySet()) {
                reverseBuilder.addReactant(product.getKey(), product.getValue());
            };
            for (Entry<LegacySpecies, Integer> rateAffecter : reaction.orders.entrySet()) {
                if (reaction.reactants.keySet().contains(rateAffecter.getKey())) continue; // Ignore reactants, only add catalysts
                reverseBuilder.addCatalyst(rateAffecter.getKey(), rateAffecter.getValue());
            };
            reaction.reverseReaction = reverseBuilder.reaction; // These settings are just for JEI's benefit
            reverseBuilder.reaction.reverseReaction = reaction;

            reverseBuilder
                .id(reaction.id + ".reverse")
                .dontIncludeInJei();

            if (hasForcedEnthalpyChange) {
                reverseBuilder.enthalpyChange(-reaction.enthalpyChange);
                if (hasForcedActivationEnergy) { // If we've set the enthalpy change and activation energy for this Reaction, the values for the reverse are set in stone
                    reverseBuilder.activationEnergy(reaction.activationEnergy - reaction.enthalpyChange);
                };
            };

            if (reaction.needsUV()) reverseBuilder.requireUV();

            if (hasForcedHalfCellPotential) reverseBuilder.standardHalfCellPotential(-reaction.standardHalfCellPotential);

            reverseReactionModifier.accept(reverseBuilder); // Allow the user to manipulate the reverse Reaction

            // Check thermodynamics are correct

            if (reaction.enthalpyChange != -reverseBuilder.reaction.enthalpyChange) { // Attempt to correct enthalpy changes
                if (!hasForcedEnthalpyChange) {
                    enthalpyChange(reaction.activationEnergy - reverseBuilder.reaction.activationEnergy);
                    reverseBuilder.enthalpyChange(-reaction.enthalpyChange);
                } else {
                    throw e("The enthalpy change of a reverse reaction must be the negative of the forward");
                };
            };

            if ( reaction.activationEnergy - reaction.enthalpyChange != reverseBuilder.reaction.activationEnergy) { // Attempt to correct activation energies
                if (!reverseBuilder.hasForcedActivationEnergy) {
                    reverseBuilder.activationEnergy(reaction.activationEnergy - reaction.enthalpyChange);
                } else if (!hasForcedActivationEnergy) {
                    activationEnergy(reverseBuilder.reaction.activationEnergy + reaction.enthalpyChange);
                } else {
                    throw e("Activation energies and enthalpy changes for reversible Reactions must obey Hess' Law");
                };
            };

            reverseBuilder.build();
            return this;
        };

        public LegacyReaction build() {

            if (reaction.id == null && !generated) {
                throw e("Reaction is missing an ID.");
            };

            // Electrochem calculations

            int chargeDecrease = 0;
            for (Entry<LegacySpecies, Integer> reactant : reaction.reactants.entrySet()) {
                chargeDecrease += reactant.getKey().getCharge() * reactant.getValue();
            };
            for (Entry<LegacySpecies, Integer> product : reaction.products.entrySet()) {
                chargeDecrease -= product.getKey().getCharge() * product.getValue();
            };
            if (chargeDecrease != 0 && chargeDecrease < 0 != declaredAsReverse) { // Reactions which were generated with `reverseReaction(r -> {})` should be oxidations
                throw e("Reactions must conserve charge or be reduction half-Reactions.");
            } else if (chargeDecrease == 0) {
                if (hasForcedHalfCellPotential) throw e("A half-cell potential is specified but electrons are not transferred.");
            } else {
                if (!hasForcedHalfCellPotential) throw e("Half-Reactions must specify a half-cell potential.");
                if (reaction.reverseReaction == null) throw e("Half-Reactions must be reversible.");
                reaction.electrons = chargeDecrease;
            };

            // Kinetics calculations

            if (!hasForcedActivationEnergy) {
                reaction.activationEnergy = 25f;
                //Destroy.LOGGER.warn("Activation energy of reaction '"+reactionString()+"' was missing or invalid, so estimated as 25kJ.");
            };

            if (!hasForcedPreExponentialFactor || reaction.preexponentialFactor <= 0f) {
                reaction.preexponentialFactor = 1e4f;
            };

            if (!hasForcedEnthalpyChange) reaction.enthalpyChange = 0f;

            if (reaction.consumesItem() && reaction.molesPerItem == 0f) {
                Destroy.LOGGER.warn("Reaction '"+reactionString()+"' does not do anything when its required Items are consumed.");
            };

            // Overhead for built-in Reactions

            if (!generated) { // Reactions generated by Generic Reaction generators and oxidation half-reactions don't get added to the list of known reactions
                for (LegacySpecies reactant : reaction.reactants.keySet()) {
                    reactant.addReactantReaction(reaction);
                };
                for (LegacySpecies product : reaction.products.keySet()) {
                    product.addProductReaction(reaction);
                };
                REACTIONS.put(reaction.getFullId(), reaction);
            };
            
            return reaction;
        };

        public boolean hasReactant(LegacySpecies reactant) {
            return reaction.reactants.containsKey(reactant);
        };

        public class ReactionConstructionException extends ChemistryException {

            public ReactionConstructionException(String message) {
                super(message);
            };
            
        };

        private ReactionConstructionException e(String message) {
            String id = reaction.id == null ? reactionString() : reaction.nameSpace + ":" + reaction.id;
            return new ReactionConstructionException("Problem generating reation ("+ id + "): " + message);
        };

        private String reactionString() {
            String reactionString = "";
            for (LegacySpecies reactant : reaction.reactants.keySet()) {
                reactionString += reactant.getSerlializedMolecularFormula(false);
                reactionString += " + ";
            };
            if (reaction.reactants.keySet().size() > 0) reactionString = reactionString.substring(0, reactionString.length() - 3);
            reactionString = reactionString + " => ";
            for (LegacySpecies product : reaction.products.keySet()) {
                reactionString += product.getSerlializedMolecularFormula(false);
                reactionString += " + ";
            };
            if (reaction.products.keySet().size() > 0) reactionString = reactionString.substring(0, reactionString.length() - 3);
            return reactionString;
        };
    };

    public static class RedoxReaction extends LegacyReaction {

        protected RedoxReaction(String nameSpace) {
            super(nameSpace);
        };

        /**
         * 
         * @param reduction The reduction half-Reaction which happens forwards
         * @param oxidation The reduction half-Reaction which occurs in reverse (as an oxidation)
         * @return
         */
        // public static RedoxReaction combine(Reaction reduction, Reaction oxidation) {
        //     if (!reduction.isHalfReaction() || !oxidation.isHalfReaction()) throw new 
        // };

    };
};
