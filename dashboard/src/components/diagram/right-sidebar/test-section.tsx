import { Label } from './label'
import { Section } from './section'

function Labels() {
  return (
    <>
      <Label label="ThisIsALongLabelWithALongNameAndLongValue">ThisIsALongLabelWithALongNameAndLongValue</Label>
      <Label label="ThisIsALongLabelWithALongNameAndLongValue2">ThisIsALongLabelWithALongNameAnd</Label>
      <Label label="ShortLabel">ShortLabel</Label>
      <Label label="ShortLabel2">ShortLabel2</Label>
      <Label label="ShrtLbl">ShrtLbl</Label>
      <Label label="ShrtLbl">ShrtLblLongValueWithALotOfTextLoremIpsumDolorSitAmetConsecteturAdipiscingElit</Label>
    </>
  )
}
export default function TestSection() {
  return (
    <Section title="RootSection">
      <Labels />
      <Section title="NestedSection">
        <Labels />
      </Section>
      <Section title="SectionWithRepeatedSections">
        {Array.from({ length: 10 }).map((_, index) => (
          <Section key={index} title={`RepeatedSection ${index}`}>
            <Labels />
          </Section>
        ))}
      </Section>
    </Section>
  )
}
