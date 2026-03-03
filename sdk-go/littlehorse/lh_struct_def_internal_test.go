package littlehorse

import (
	"reflect"
	"testing"

	"github.com/stretchr/testify/assert"
)

// --- Tests for parseLHTag ---
// parseLHTag is the core tag parser for the "lh" struct tag. These tests cover
// all supported tag formats and edge cases.

func TestParseLHTag_EmptyTag(t *testing.T) {
	// An empty tag should return zero values: no name, not masked, not skipped.
	info := parseLHTag("")
	assert.Equal(t, "", info.name)
	assert.False(t, info.masked)
	assert.False(t, info.skip)
}

func TestParseLHTag_NameOnly(t *testing.T) {
	// A tag with just a name should set the name but nothing else.
	info := parseLHTag("firstName")
	assert.Equal(t, "firstName", info.name)
	assert.False(t, info.masked)
	assert.False(t, info.skip)
}

func TestParseLHTag_SkipTag(t *testing.T) {
	// A tag of "-" means the field should be skipped entirely.
	info := parseLHTag("-")
	assert.True(t, info.skip)
	assert.Equal(t, "", info.name)
	assert.False(t, info.masked)
}

func TestParseLHTag_NameAndMasked(t *testing.T) {
	// A tag of "ssn,masked" sets both the name and the masked flag.
	info := parseLHTag("ssn,masked")
	assert.Equal(t, "ssn", info.name)
	assert.True(t, info.masked)
	assert.False(t, info.skip)
}

func TestParseLHTag_EmptyNameAndMasked(t *testing.T) {
	// A tag of ",masked" sets masked but leaves the name empty (fall back to default).
	info := parseLHTag(",masked")
	assert.Equal(t, "", info.name)
	assert.True(t, info.masked)
	assert.False(t, info.skip)
}

func TestParseLHTag_MaskedWithWhitespace(t *testing.T) {
	// Whitespace around the "masked" option should be trimmed.
	info := parseLHTag("ssn, masked")
	assert.Equal(t, "ssn", info.name)
	assert.True(t, info.masked)
}

func TestParseLHTag_UnknownOptionsIgnored(t *testing.T) {
	// Unknown options should be silently ignored (forward-compatible).
	info := parseLHTag("field,unknown,masked,another")
	assert.Equal(t, "field", info.name)
	assert.True(t, info.masked)
	assert.False(t, info.skip)
}

func TestParseLHTag_OnlyUnknownOptions(t *testing.T) {
	// A tag with only unknown options should not set masked or skip.
	info := parseLHTag("field,unknown,future_option")
	assert.Equal(t, "field", info.name)
	assert.False(t, info.masked)
	assert.False(t, info.skip)
}

func TestParseLHTag_SkipDoesNotParseOptions(t *testing.T) {
	// The skip tag "-" is a special case: it is handled before splitting on commas.
	// A tag of "-" should always skip, regardless of any apparent "options".
	// Note: "-,masked" is NOT "-"; it's a name of "-" with masked, which is an unusual
	// edge case. The current behavior treats only exactly "-" as skip.
	info := parseLHTag("-,masked")
	assert.False(t, info.skip, "only exactly '-' should skip, not '-,masked'")
	assert.Equal(t, "-", info.name)
	assert.True(t, info.masked)
}

// --- Tests for resolveFieldName ---
// resolveFieldName determines the final field name using a priority chain:
// lh tag name > json tag name > PascalCase-to-camelCase conversion.

func TestResolveFieldName_LHNameTakesPriority(t *testing.T) {
	// When the lh tag provides a name, it wins over everything else.
	field := reflect.StructField{
		Name: "MyField",
		Tag:  `lh:"custom_name" json:"json_name"`,
	}
	assert.Equal(t, "custom_name", resolveFieldName("custom_name", field))
}

func TestResolveFieldName_FallsBackToJSONTag(t *testing.T) {
	// When lh name is empty, fall back to the json tag.
	field := reflect.StructField{
		Name: "MyField",
		Tag:  `json:"json_name"`,
	}
	assert.Equal(t, "json_name", resolveFieldName("", field))
}

func TestResolveFieldName_FallsBackToCamelCase(t *testing.T) {
	// When both lh and json names are empty, use PascalCase-to-camelCase.
	field := reflect.StructField{
		Name: "MyFieldName",
		Tag:  "",
	}
	assert.Equal(t, "myFieldName", resolveFieldName("", field))
}

func TestResolveFieldName_JSONTagWithCommaOptions(t *testing.T) {
	// JSON tag options after the comma (like "omitempty") should not affect the name.
	field := reflect.StructField{
		Name: "MyField",
		Tag:  `json:"json_name,omitempty"`,
	}
	assert.Equal(t, "json_name", resolveFieldName("", field))
}

func TestResolveFieldName_EmptyJSONTagFallsToCamelCase(t *testing.T) {
	// A json tag that resolves to an empty name (e.g., json:",omitempty") should
	// fall through to camelCase conversion.
	field := reflect.StructField{
		Name: "MyField",
		Tag:  `json:",omitempty"`,
	}
	assert.Equal(t, "myField", resolveFieldName("", field))
}

// --- Tests for goFieldNameToCamelCase ---
// goFieldNameToCamelCase converts Go PascalCase to camelCase. These tests
// cover standard conversions and edge cases.

func TestGoFieldNameToCamelCase_SingleWord(t *testing.T) {
	assert.Equal(t, "name", goFieldNameToCamelCase("Name"))
}

func TestGoFieldNameToCamelCase_TwoWords(t *testing.T) {
	assert.Equal(t, "firstName", goFieldNameToCamelCase("FirstName"))
}

func TestGoFieldNameToCamelCase_AllCaps(t *testing.T) {
	// "HTTP" -> "http" (all uppercase, lowercase all)
	assert.Equal(t, "http", goFieldNameToCamelCase("HTTP"))
}

func TestGoFieldNameToCamelCase_AcronymFollowedByWord(t *testing.T) {
	// "HTTPServer" -> "httpServer" (acronym before a word)
	assert.Equal(t, "httpServer", goFieldNameToCamelCase("HTTPServer"))
}

func TestGoFieldNameToCamelCase_AlreadyLowercase(t *testing.T) {
	assert.Equal(t, "already", goFieldNameToCamelCase("already"))
}

func TestGoFieldNameToCamelCase_EmptyString(t *testing.T) {
	assert.Equal(t, "", goFieldNameToCamelCase(""))
}

func TestGoFieldNameToCamelCase_SingleChar(t *testing.T) {
	assert.Equal(t, "x", goFieldNameToCamelCase("X"))
}

func TestGoFieldNameToCamelCase_SingleLowerChar(t *testing.T) {
	assert.Equal(t, "x", goFieldNameToCamelCase("x"))
}

func TestGoFieldNameToCamelCase_ThreeWordField(t *testing.T) {
	assert.Equal(t, "vinNumberISO3779", goFieldNameToCamelCase("VinNumberISO3779"))
}

// --- Tests for LHStructDefInfo ---
// LHStructDefInfo is the struct returned by the LHStructDef() method. These tests
// ensure Name and Description are correctly propagated through the SDK.

type StructWithDescription struct {
	Value string `json:"value"`
}

func (StructWithDescription) LHStructDef() LHStructDefInfo {
	return LHStructDefInfo{
		Name:        "described-struct",
		Description: "A test struct with a description",
	}
}

func TestGetStructDefInfo_ReturnsNameAndDescription(t *testing.T) {
	info := getStructDefInfo(reflect.TypeOf(StructWithDescription{}))
	assert.NotNil(t, info)
	assert.Equal(t, "described-struct", info.Name)
	assert.Equal(t, "A test struct with a description", info.Description)
}

func TestGetStructDefInfo_ReturnsNilForPlainStruct(t *testing.T) {
	type PlainStruct struct {
		Value string
	}
	info := getStructDefInfo(reflect.TypeOf(PlainStruct{}))
	assert.Nil(t, info)
}

func TestGetStructDefName_ReturnsName(t *testing.T) {
	name := getStructDefName(reflect.TypeOf(StructWithDescription{}))
	assert.Equal(t, "described-struct", name)
}

func TestGetStructDefName_ReturnsEmptyForPlainStruct(t *testing.T) {
	type PlainStruct struct {
		Value string
	}
	name := getStructDefName(reflect.TypeOf(PlainStruct{}))
	assert.Equal(t, "", name)
}
