package llvm;

import java.util.HashSet;
import java.util.Set;

/**
 * enum containing all possible values for llvm::dwarf::Tag. Full list can be obtained using printAllTags in the JNIHelpers.cpp file
 */
public enum DwarfTag {
    //added _ to null manually because 'null' is a keyword in java
    _null(0x0),
    array_type(0x1),
    class_type(0x2),
    entry_point(0x3),
    enumeration_type(0x4),
    formal_parameter(0x5),
    imported_declaration(0x8),
    label(0xa),
    lexical_block(0xb),
    member(0xd),
    pointer_type(0xf),
    reference_type(0x10),
    compile_unit(0x11),
    string_type(0x12),
    structure_type(0x13),
    subroutine_type(0x15),
    typedef(0x16),
    union_type(0x17),
    unspecified_parameters(0x18),
    variant(0x19),
    common_block(0x1a),
    common_inclusion(0x1b),
    inheritance(0x1c),
    inlined_subroutine(0x1d),
    module(0x1e),
    ptr_to_member_type(0x1f),
    set_type(0x20),
    subrange_type(0x21),
    with_stmt(0x22),
    access_declaration(0x23),
    base_type(0x24),
    catch_block(0x25),
    const_type(0x26),
    constant(0x27),
    enumerator(0x28),
    file_type(0x29),
    friend(0x2a),
    namelist(0x2b),
    namelist_item(0x2c),
    packed_type(0x2d),
    subprogram(0x2e),
    template_type_parameter(0x2f),
    template_value_parameter(0x30),
    thrown_type(0x31),
    try_block(0x32),
    variant_part(0x33),
    variable(0x34),
    volatile_type(0x35),
    dwarf_procedure(0x36),
    restrict_type(0x37),
    interface_type(0x38),
    namespace(0x39),
    imported_module(0x3a),
    unspecified_type(0x3b),
    partial_unit(0x3c),
    imported_unit(0x3d),
    condition(0x3f),
    shared_type(0x40),
    type_unit(0x41),
    rvalue_reference_type(0x42),
    template_alias(0x43),
    coarray_type(0x44),
    generic_subrange(0x45),
    dynamic_type(0x46),
    atomic_type(0x47),
    call_site(0x48),
    call_site_parameter(0x49),
    skeleton_unit(0x4a),
    immutable_type(0x4b),
    MIPS_loop(0x4081),
    format_label(0x4101),
    function_template(0x4102),
    class_template(0x4103),
    GNU_BINCL(0x4104),
    GNU_EINCL(0x4105),
    GNU_template_template_param(0x4106),
    GNU_template_parameter_pack(0x4107),
    GNU_formal_parameter_pack(0x4108),
    GNU_call_site(0x4109),
    GNU_call_site_parameter(0x410a),
    APPLE_property(0x4200),
    SUN_function_template(0x4201),
    SUN_class_template(0x4202),
    SUN_struct_template(0x4203),
    SUN_union_template(0x4204),
    SUN_indirect_inheritance(0x4205),
    SUN_codeflags(0x4206),
    SUN_memop_info(0x4207),
    SUN_omp_child_func(0x4208),
    SUN_rtti_descriptor(0x4209),
    SUN_dtor_info(0x420a),
    SUN_dtor(0x420b),
    SUN_f90_interface(0x420c),
    SUN_fortran_vax_structure(0x420d),
    SUN_hi(0x42ff),
    LLVM_ptrauth_type(0x4300),
    ALTIUM_circ_type(0x5101),
    ALTIUM_mwa_circ_type(0x5102),
    ALTIUM_rev_carry_type(0x5103),
    ALTIUM_rom(0x5111),
    LLVM_annotation(0x6000),
    GHS_namespace(0x8004),
    GHS_using_namespace(0x8005),
    GHS_using_declaration(0x8006),
    GHS_template_templ_param(0x8007),
    UPC_shared_type(0x8765),
    UPC_strict_type(0x8766),
    UPC_relaxed(0x8767),
    PGI_kanji_type(0xa000),
    PGI_interface_block(0xa020),
    BORLAND_property(0xb000),
    BORLAND_Delphi_string(0xb001),
    BORLAND_Delphi_dynamic_array(0xb002),
    BORLAND_Delphi_set(0xb003),
    BORLAND_Delphi_variant(0xb004),
    lo_user(0x4080),
    hi_user(0xffff),
    user_base(0x1000);

    // The field that holds the assigned value
    private final int value;
    DwarfTag(int value) {
        this.value = value;
    }
    
    public boolean isTypeTag() {
        Set<DwarfTag> TypeTags = Set.of(
                DwarfTag.base_type,
                DwarfTag.pointer_type,
                DwarfTag.const_type,
                DwarfTag.volatile_type,
                DwarfTag.restrict_type,
                DwarfTag.typedef,
                DwarfTag.array_type,
                DwarfTag.subroutine_type,
                DwarfTag.structure_type,
                DwarfTag.union_type,
                DwarfTag.enumeration_type,
                DwarfTag.subrange_type,
                DwarfTag.ptr_to_member_type,
                DwarfTag.unspecified_type
        );
        System.out.println("Checking if " + this + " is a type Tag.");
        return TypeTags.contains(this);
    }
    public int getValue() {
        return value;
    }
    public static DwarfTag fromValue(int v) {
        //TODO: make this more efficient
        for (DwarfTag tag : values()) {
            if (tag.value == v) return tag;
        }
        //I know this a weird approach code but llvm does not list the enum, but I don't want to deal with just using
        // integers instead of more readable enum names.
        throw new RuntimeException("Encountered an unknown DwarfTag. Perhaps update '/src/main/java/DwarfTag.java' to" +
                "contain all tag values, that dwarf can produce. llvm_DWARFContext.cpp contains the printAllTags" +
                "function which can be used to output a copy-pastable list into the DwarfTag.java enum.");
    }
}
