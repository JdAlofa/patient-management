package com.pm.patient_service.dto.validators;

public interface CreatePatientValidationGroup {
}

/**
 * Validation Group Marker Interface for Patient Creation Operations
 * 
 * PURPOSE:
 * --------
 * This is a "marker interface" (empty interface with no methods) used to group
 * validation constraints that should ONLY apply when CREATING a new patient,
 * but NOT when UPDATING an existing patient.
 * 
 * PROBLEM IT SOLVES:
 * -------------------
 * We use the same PatientRequestDTO for both CREATE (POST) and UPDATE (PUT)
 * operations.
 * However, some fields have different validation requirements:
 * 
 * - CREATE: registeredDate is REQUIRED (it's when the patient was registered)
 * - UPDATE: registeredDate should be OPTIONAL (we don't want to change
 *   registration date)
 * 
 * Without validation groups, we'd need either:
 * 1. Two separate DTOs (CreatePatientDTO and UpdatePatientDTO) - more code
 *    duplication
 * 2. No validation on registeredDate - less safe
 * 3. Always require registeredDate on updates - awkward API design
 * 
 * HOW IT WORKS:
 * -------------
 * 1. In PatientRequestDTO.java, we tag the registeredDate field:
 * 
 *    @NotBlank(groups = CreatePatientValidationGroup.class, message = "Registered
 *              date is required")
 *    private String registeredDate;
 * 
 *    This means: "Only validate this field when CreatePatientValidationGroup is specified"
 * 
 * 2. In PatientController.java, we specify which groups to validate:
 * 
 *    CREATE endpoint:
 *        @PostMapping
 *        public ResponseEntity<PatientResponseDTO> createPatient(
 *            @Validated({ Default.class, CreatePatientValidationGroup.class }) // ← Validates BOTH groups
 *            @RequestBody PatientRequestDTO dto) { ... }
 * 
 *    UPDATE endpoint:
 *        @PutMapping("/{id}")
 *        public ResponseEntity<PatientResponseDTO> updatePatient(
 *            @Validated(Default.class) // ← Only validates Default group (skips CreatePatientValidationGroup)
 *            @RequestBody PatientRequestDTO dto) { ... }
 * 
 *    VALIDATION BEHAVIOR:
 *    --------------------
 * 
 *    Field           | Validation Group                         | CREATE (POST) | UPDATE (PUT)
 *    --------------- | ---------------------------------------- | ------------- | -------------
 *    name            | Default (implicit)                       | ✅ Required   | ✅ Required
 *    email           | Default (implicit)                       | ✅ Required   | ✅ Required
 *    address         | Default (implicit)                       | ✅ Required   | ✅ Required
 *    dateOfBirth     | Default (implicit)                       | ✅ Required   | ✅ Required
 *    registeredDate  | CreatePatientValidationGroup             | ✅ Required   | ⏭️ Optional (not validated)
 * 
 *    ABOUT Default.class:
 *    --------------------
 *    When you don't specify a 'groups' attribute on a constraint, it automatically
 *    belongs to the Default validation group:
 * 
 *    @NotBlank(message = "Name is required") // ← Implicitly in Default.class
 *    private String name;
 */
