package org.exist.dom.persistent;

import org.apache.log4j.Logger;
import org.exist.dom.*;
import org.exist.storage.txn.Txn;
import org.w3c.dom.DOMException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.UserDataHandler;

public abstract class NodeImpl<T extends NodeImpl> implements INode<DocumentImpl, T> {

    protected final static Logger LOG = Logger.getLogger(NodeImpl.class);    

    /**
     * @see org.w3c.dom.Node#cloneNode(boolean)
     */
    @Override
    public Node cloneNode(boolean deep) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
     */
    @Override
    public Node appendChild(Node child) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    public void updateChild(Node oldChild, Node newChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
     */
    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    public Node insertAfter(Node newChild, Node refChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    public void appendChildren(Txn transaction, NodeList nodes, int child) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    public Node removeChild(Txn transaction, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }
 
    public Node replaceChild(Txn transaction, Node newChild, Node oldChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

    /**
     * Update a child node. This method will only update the child node
     * but not its potential descendant nodes.
     * 
     * @param oldChild
     * @param newChild
     * @throws DOMException
     */
    public IStoredNode updateChild(Txn transaction, Node oldChild, Node newChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "not implemented on class " + getClass().getName());
    }

  /**
   * Insert a list of nodes at the position before the reference
   * child.
   *
   * NOTE: You must call insertBefore on the parent node of the node that you
   * want to insert nodes before.
   */
  public void insertBefore(Txn transaction, NodeList nodes, Node refChild) throws DOMException {
    throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
      "not implemented on class " + getClass().getName());
  }

  /**
   * Insert a list of nodes at the position following the reference
   * child.
   *
   * NOTE: You must call insertAfter on the parent node of the node that you want
   * to insert nodes after.
   */
    public void insertAfter(Txn transaction, NodeList nodes, Node refChild) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "insertAfter(Txn transaction, NodeList nodes, Node refChild) " +
            "not implemented on class " + getClass().getName());
    } 

    public int getChildCount() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getChildCount() not implemented on class " + getClass().getName());
    }

    @Override
    public NodeList getChildNodes() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getChildNodes() not implemented on class " + getClass().getName());
    }

	/**
	 * @see org.w3c.dom.Node#getFirstChild()
	 */
    @Override
    public Node getFirstChild() {       
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getFirstChild() not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#getLastChild()
     */
    @Override
    public Node getLastChild() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getLastChild() not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#hasAttributes()
     */
    @Override
    public boolean hasAttributes() {
        return getAttributesCount() > 0;
    }

    public short getAttributesCount() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getAttributesCount() not implemented on class " + getClass().getName());
    }    

    /**
     * @see org.w3c.dom.Node#getAttributes()
     */
    @Override
    public NamedNodeMap getAttributes() {
        return null;
    }

    /**
     *  Set the attributes that belong to this node.
     *
     * @param  attribNum  The new attributes value
     */
    public void setAttributes(short attribNum) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "setAttributes(short attribNum) not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#getNodeValue()
     */
    @Override
    public String getNodeValue() throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getNodeValue() not implemented on class " + getClass().getName());
    } 

    /**
     *  Set the node value.
     *
     *@param  value             The new nodeValue value
     *@exception  DOMException  Description of the Exception
     */
    @Override
    public void setNodeValue(String value) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "setNodeValue(String value) not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#hasChildNodes()
     */
    @Override
    public boolean hasChildNodes() {
        return getChildCount() > 0;
    } 

    /**
     *  Set the number of children.
     *
     *@param  count  The new childCount value
     */
    protected void setChildCount(int count) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "setChildCount(int count) not implemented on class " + getClass().getName());
    }

    /**
     *  Set the node name.
     *
     *@param  name  The new nodeName value
     */
    public void setNodeName(QName name) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "setNodeName(QName name) not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
     */
    @Override
    public boolean isSupported(String key, String value) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "isSupported(String key, String value) not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#normalize()
     */
    @Override
    public void normalize() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "normalize() not implemented on class " + getClass().getName());
    }  

    /**
     * Method supports.
     * @param feature
     * @param version
     * @return boolean
     */
    public boolean supports(String feature, String version) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "supports(String feature, String version) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#getBaseURI()
     */
    @Override
    public String getBaseURI() {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getBaseURI() not implemented on class " + getClass().getName());
    }

//    protected XmldbURI calculateBaseURI() {
//        return null;
//    }

    /** ? @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
     */
    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "compareDocumentPosition(Node other) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#getTextContent()
     */
    @Override
    public String getTextContent() throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getTextContent() not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#setTextContent(java.lang.String)
     */
    @Override
    public void setTextContent(String textContent) throws DOMException {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "setTextContent(String textContent) not implemented on class " + getClass().getName());		
    }

    /** ? @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
     */
    @Override
    public boolean isSameNode(Node other) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "isSameNode(Node other) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
     */
    @Override
    public String lookupPrefix(String namespaceURI) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "lookupPrefix(String namespaceURI) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
     */
    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "isDefaultNamespace(String namespaceURI) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
     */
    @Override
    public String lookupNamespaceURI(String prefix) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "lookupNamespaceURI(String prefix) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
     */
    @Override
    public boolean isEqualNode(Node arg) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "isEqualNode(Node arg) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
     */
    @Override
    public Object getFeature(String feature, String version) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getFeature(String feature, String version) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#getUserData(java.lang.String)
     */
    @Override
    public Object getUserData(String key) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "getUserData(String key) not implemented on class " + getClass().getName());
    }

    /** ? @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object, org.w3c.dom.UserDataHandler)
     */
    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR,
            "setUserData(String key, Object data, UserDataHandler handler) not implemented on class " + getClass().getName());
    }

    /**
     * @see org.w3c.dom.Node#getPrefix()
     */
    @Override
    public String getPrefix() {
        final QName nodeName = getQName();
        final String prefix = nodeName.getPrefix();
        return prefix == null ? "" : prefix;
    }

    /**
     *  Sets the prefix attribute of the NodeImpl object
     *
     *@param  prefix            The new prefix value
     *@exception  DOMException  Description of the Exception
     */
    @Override
    public void setPrefix(String prefix) throws DOMException {
        final QName nodeName = getQName();
        if (nodeName != null) {
            setQName(new QName(nodeName.getLocalPart(), nodeName.getNamespaceURI(), prefix));
        }
    }

    /**
     * @see org.w3c.dom.Node#getNamespaceURI()
     */
    //TODO : remove default value
    @Override
    public String getNamespaceURI() {
        return getQName().getNamespaceURI();
    }

    /**
     * @see org.w3c.dom.Node#getLocalName()
     */
    //TODO : remove default value
    @Override
    public String getLocalName() {
        return getQName().getLocalPart();
    }

    /**
     * @see org.w3c.dom.Node#getNodeName()
     */
    @Override
    public String getNodeName() {
        return getQName().getStringValue();
    }
}
