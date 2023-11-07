import LabelsUtils from './LabelsUtils'

describe('labels Utils', () => {
  it('should extract label to be shown in the the screen from node name', () => {
    const label = LabelsUtils.extractLabel('2-exit-EXIT')

    expect(label).toEqual('exit')
  })
})